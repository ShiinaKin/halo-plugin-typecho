package io.sakurasou.halo.typecho.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.sakurasou.halo.typecho.entity.RawMetaData
import io.sakurasou.halo.typecho.util.HttpUtils
import io.sakurasou.halo.typecho.util.JSON_MAPPER
import io.sakurasou.halo.typecho.util.PinyinUtils
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.springframework.stereotype.Service
import run.halo.app.core.extension.content.Category
import run.halo.app.core.extension.content.Post
import run.halo.app.core.extension.content.Post.*
import run.halo.app.core.extension.content.Tag
import run.halo.app.core.extension.content.Tag.TagSpec
import run.halo.app.extension.Metadata
import java.io.IOException
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
    private val patServiceImpl: PATServiceImpl
) {

    private val logger = KotlinLogging.logger { this::class.java }
    private val mdParser = Parser.builder().build()
    private val htmlRenderer = HtmlRenderer.builder().build()

    fun handlePosts(postGroupByCategory: Map<String, List<Pair<RawMetaData, String>>>) {
        val categories = handleListCategories().toMutableMap()
        val tags = handleListTags().toMutableMap()

        for ((categoryName, posts) in postGroupByCategory) {
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
                handleCreateCategory(category)
                categories[categoryName] = categoryPinyinNamed
            }

            for ((rawMetaData, mdContent) in posts) {
                val rawTags = rawMetaData.tags
                rawTags?.filterNot { tags.containsKey(it) }?.forEach {
                    val tagPinyinNamed = PinyinUtils.trans2Pinyin(it)
                    val tag = Tag().apply {
                        spec = generateTagSpec(it, tagPinyinNamed)
                        apiVersion = CONTENT_HALO_RUN
                        kind = "Tag"
                        metadata = Metadata().apply {
                            name = tagPinyinNamed
                            generateName = "tag-"
                        }
                    }
                    handleCreateTag(tag)
                    tags[it] = tagPinyinNamed
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

                handleCreatePost(post, content)
            }
        }
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
        val pat = patServiceImpl.getPAT()
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
        val pat = patServiceImpl.getPAT()
        val createCategoryUrl = "http://localhost:8090/apis/content.halo.run/v1alpha1/categories"
        val jsonBody = JSON_MAPPER.writeValueAsString(category)

        HttpUtils.sendPostReq(createCategoryUrl, jsonBody, pat)
    }

    private fun handleCreateTag(tag: Tag) {
        val pat = patServiceImpl.getPAT()
        val createTagUrl = "http://localhost:8090/apis/content.halo.run/v1alpha1/tags"
        val jsonBody = JSON_MAPPER.writeValueAsString(tag)

        HttpUtils.sendPostReq(createTagUrl, jsonBody, pat)
    }

    @Throws(IOException::class)
    private fun handleCreatePost(post: Post, content: Content) {
        val pat = patServiceImpl.getPAT()
        val draftPostUrl = "http://localhost:8090/apis/api.console.halo.run/v1alpha1/posts"
        val postRequest = PostRequest(post, content)
        val jsonBody = JSON_MAPPER.writeValueAsString(postRequest)

        HttpUtils.sendPostReq(draftPostUrl, jsonBody, pat)
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
            publishTime = Instant.now()
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

    data class PostRequest(val post: Post, val content: Content)

    data class Content(val raw: String, val content: String, val rawType: String)
}
