package io.sakurasou.halo.typecho.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.sakurasou.halo.typecho.entity.RawMetaData
import io.sakurasou.halo.typecho.util.HttpUtils
import io.sakurasou.halo.typecho.util.JSON_MAPPER
import io.sakurasou.halo.typecho.util.PAGE
import io.sakurasou.halo.typecho.util.PinyinUtils
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.springframework.stereotype.Service
import run.halo.app.core.extension.content.Category
import run.halo.app.core.extension.content.Post
import run.halo.app.core.extension.content.Post.*
import run.halo.app.core.extension.content.SinglePage
import run.halo.app.core.extension.content.Tag
import run.halo.app.core.extension.content.Tag.TagSpec
import run.halo.app.extension.Metadata
import java.time.Instant
import java.util.*

private const val MARKDOWN = "markdown"
private const val CONTENT_HALO_RUN = "content.halo.run/v1alpha1"

/**
 * @author mashirot
 * 2024/5/7 20:33
 */
@Service
class UploadServiceImpl(
    configServiceImpl: ConfigServiceImpl
) {

    private val logger = KotlinLogging.logger { this::class.java }
    private val extensions = listOf(TablesExtension.create(), HeadingAnchorExtension.create())
    private val mdParser = Parser.builder().extensions(extensions).build()
    private val htmlRenderer = HtmlRenderer.builder().extensions(extensions).build()
    private val pat = configServiceImpl.getPAT()
    private val zoneOffset = configServiceImpl.getTimeZone().rules.getOffset(Instant.now())

    fun handlePages(pageMap: Map<String, List<Pair<RawMetaData, String>>>): Pair<Int, Int> {
        val pages = pageMap[PAGE] ?: return 0 to 0
        var succeedCnt = 0
        for ((rawMetaData, pageContent) in pages) {
            val page = SinglePage().apply {
                spec = generatePageSpec(rawMetaData)
                apiVersion = CONTENT_HALO_RUN
                kind = "SinglePage"
                metadata = Metadata().apply {
                    name = UUID.randomUUID().toString()
                    annotations = mapOf("content.halo.run/preferred-editor" to "vditor-mde")
                }
            }
            runCatching {
                val document = mdParser.parse(pageContent)
                val content = Content(pageContent, htmlRenderer.render(document), MARKDOWN)

                val result = handleCreatePage(page, content)
                if (result) succeedCnt++
                else logger.warn { "page: ${rawMetaData.title} create failed, more details pls see halo logs" }
            }.getOrElse {
                logger.warn { "page: ${rawMetaData.title} create failed: ${it.message}" }
            }
        }
        return pages.size to succeedCnt
    }

    fun handlePosts(postGroupByCategory: Map<String, List<Pair<RawMetaData, String>>>): Pair<Int, Int> {
        val categories = handleListCategories().toMutableMap()
        val tags = handleListTags().toMutableMap()

        var totalPostCnt = 0
        var succeedCnt = 0

        postGroupByCategory.keys.forEach { categoryName ->
            val categoryPinyinNamed = PinyinUtils.trans2Pinyin(categoryName)
            if (!categories.containsKey(categoryName)) {
                val category = Category().apply {
                    spec = generateCategorySpec(categoryName, categoryPinyinNamed)
                    status = Category.CategoryStatus()
                    apiVersion = CONTENT_HALO_RUN
                    kind = "Category"
                    metadata = Metadata().apply {
                        name = categoryPinyinNamed
                        generateName = "category-"
                    }
                }
                try {
                    handleCreateCategory(category)
                    categories[categoryName] = categoryPinyinNamed
                } catch (e: Exception) {
                    logger.warn { "category: $categoryName create failed: ${e.message}" }
                }
            }
        }

        val allPost = postGroupByCategory.flatMap { entry -> entry.value.map { entry.key to it } }
            .sortedBy { it.second.first.date }

        totalPostCnt += allPost.size

        allPost.forEach { (categoryName, posts) ->
            if (!categories.containsKey(categoryName)) return@forEach

            val (rawMetaData, mdContent) = posts
            val rawTags = rawMetaData.tags

            rawTags?.filterNot { tags.containsKey(it) }?.forEach { tagName ->
                val tagPinyinNamed = PinyinUtils.trans2Pinyin(tagName)
                val tag = Tag().apply {
                    spec = generateTagSpec(tagName, tagPinyinNamed)
                    apiVersion = CONTENT_HALO_RUN
                    kind = "Tag"
                    metadata = Metadata().apply {
                        name = tagPinyinNamed
                        generateName = "tag-"
                    }
                }
                runCatching {
                    handleCreateTag(tag)
                    tags[tagName] = tagPinyinNamed
                }.getOrElse {
                    logger.warn { "tag: $tagName create failed: ${it.message}" }
                }
            }

            val post = Post().apply {
                spec = generatePostSpec(rawMetaData, categories, tags)
                apiVersion = CONTENT_HALO_RUN
                kind = "Post"
                metadata = Metadata().apply {
                    name = UUID.randomUUID().toString()
                }
            }
            val document = mdParser.parse(mdContent)
            val content = Content(mdContent, htmlRenderer.render(document), MARKDOWN)

            runCatching {
                val result = handleCreatePost(post, content)
                if (result) succeedCnt++
                else logger.warn { "post: ${rawMetaData.title} create failed, more details pls see halo logs" }
            }.getOrElse {
                logger.warn { "post: ${rawMetaData.title} create failed: ${it.message}" }
            }
        }

        return totalPostCnt to succeedCnt
    }

    private fun handleCreatePage(page: SinglePage, content: Content): Boolean {
        val draftPostUrl = "http://localhost:8090/apis/api.console.halo.run/v1alpha1/singlepages"
        val postRequest = PageRequest(page, content)
        val jsonBody = JSON_MAPPER.writeValueAsString(postRequest)

        return HttpUtils.sendPostReq(draftPostUrl, jsonBody, pat)
    }

    private fun handleListCategories(): Map<String, String> {
        val listCategoriesUrl = "http://localhost:8090/apis/content.halo.run/v1alpha1/categories?page=1&size=500"
        return handleListTagsOrCategories(listCategoriesUrl)
    }

    private fun handleListTags(): Map<String, String> {
        val listCategoriesUrl = "http://localhost:8090/apis/api.console.halo.run/v1alpha1/tags?page=1&size=500"
        return handleListTagsOrCategories(listCategoriesUrl)
    }

    private fun handleListTagsOrCategories(url: String): Map<String, String> {
        val respJson = HttpUtils.sendGetReq(url, pat)
        val items = JSON_MAPPER.readValue(respJson, Map::class.java)["items"] as List<*>
        return items.associate {
            it as Map<*, *>
            val spec = it["spec"] as Map<*, *>
            val displayName = spec["displayName"] as String
            val metadata = it["metadata"] as Map<*, *>
            val name = metadata["name"] as String
            displayName to name
        }
    }

    private fun handleCreateCategory(category: Category) {
        val createCategoryUrl = "http://localhost:8090/apis/content.halo.run/v1alpha1/categories"
        val jsonBody = JSON_MAPPER.writeValueAsString(category)

        HttpUtils.sendPostReq(createCategoryUrl, jsonBody, pat)
    }

    private fun handleCreateTag(tag: Tag) {
        val createTagUrl = "http://localhost:8090/apis/content.halo.run/v1alpha1/tags"
        val jsonBody = JSON_MAPPER.writeValueAsString(tag)

        HttpUtils.sendPostReq(createTagUrl, jsonBody, pat)
    }

    private fun handleCreatePost(post: Post, content: Content): Boolean {
        val draftPostUrl = "http://localhost:8090/apis/api.console.halo.run/v1alpha1/posts"
        val postRequest = PostRequest(post, content)
        val jsonBody = JSON_MAPPER.writeValueAsString(postRequest)

        return HttpUtils.sendPostReq(draftPostUrl, jsonBody, pat)
    }

    private fun generatePageSpec(metaData: RawMetaData) = SinglePage.SinglePageSpec().apply {
        title = metaData.title
        slug = metaData.slug
        template = ""
        cover = ""
        deleted = false
        publish = true
        pinned = false
        allowComment = false
        visible = VisibleEnum.PUBLIC
        priority = 0
        excerpt = Excerpt().apply {
            autoGenerate = true
            raw = ""
        }
        htmlMetas = listOf()
    }

    private fun generateTagSpec(tagName: String, tagPinyinNamed: String) = TagSpec().apply {
        displayName = tagName
        slug = tagPinyinNamed
        color = "#ffffff"
        cover = ""
    }

    private fun generateCategorySpec(categoryName: String, categoryPinyinNamed: String) =
        Category.CategorySpec().apply {
            displayName = categoryName
            slug = categoryPinyinNamed
            description = ""
            cover = ""
            template = ""
            priority = 0
            children = listOf()
        }

    private fun generatePostSpec(metaData: RawMetaData, categoryMap: Map<String, String>, tagMap: Map<String, String>) =
        PostSpec().apply {
            title = metaData.title
            slug = metaData.slug
            deleted = false
            publish = true
            publishTime = metaData.date.toInstant(zoneOffset)
            pinned = false
            allowComment = true
            visible = VisibleEnum.PUBLIC
            priority = 0
            excerpt = Excerpt().apply {
                autoGenerate = metaData.customSummary.isNullOrBlank()
                raw = if (metaData.customSummary.isNullOrBlank()) ""
                else metaData.customSummary
            }
            categories = metaData.categories?.map { categoryMap[it] } ?: emptyList()
            tags = metaData.tags?.map { tagMap[it] } ?: emptyList()
            htmlMetas = emptyList()
        }

    data class PageRequest(val page: SinglePage, val content: Content)

    data class PostRequest(val post: Post, val content: Content)

    data class Content(val raw: String, val content: String, val rawType: String)
}
