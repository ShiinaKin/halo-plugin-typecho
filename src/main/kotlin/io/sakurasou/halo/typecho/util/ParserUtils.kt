package io.sakurasou.halo.typecho.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.sakurasou.halo.typecho.entity.RawMetaData
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.function.*
import java.util.stream.*

const val PAGE = "page"
const val POST = "post"
val JSON_MAPPER: ObjectMapper = ObjectMapper().registerModules(JavaTimeModule(), kotlinModule())

/**
 * @author mashirot
 * 2024/5/6 22:26
 */
object ParserUtils {
    private val YAML_MAPPER = YAMLMapper().registerKotlinModule()
    private val TYPE_ARR_MD = arrayOf("md")

    @Throws(IOException::class)
    fun traverseFolder(folder: File): Map<String, Map<String, List<Pair<RawMetaData, String>>>> {
        val files = folder.listFiles() ?: throw RuntimeException("wrong file struct")
        val rootFolder = if (files.size < 2) files[0] else folder
        val result: MutableMap<String, Map<String, List<Pair<RawMetaData, String>>>> = HashMap(3)

        // page
        val pageRootFolder = File(rootFolder, PAGE)
        val pages = mapOf(PAGE to parseFolder(pageRootFolder))
        result[PAGE] = pages

        // post
        val postRootFile = File(rootFolder, POST)
        val postCategoryFiles = Stream.of(*postRootFile.listFiles())
            .collect(Collectors.toMap(
                { obj: File -> obj.name }, { file: File -> file })
            )
        val postGroupByCategory: MutableMap<String, List<Pair<RawMetaData, String>>> =
            HashMap(postCategoryFiles.size + 1)
        for ((categoryName, categoryFolder) in postCategoryFiles) {
            postGroupByCategory[categoryName] = parseFolder(categoryFolder)
        }

        result[POST] = postGroupByCategory
        return result
    }

    @Throws(IOException::class)
    private fun parseFolder(folder: File): List<Pair<RawMetaData, String>> {
        val files = ArrayList(
            FileUtils.listFiles(folder, TYPE_ARR_MD, true)
        )
        val parseResult: MutableList<Pair<RawMetaData, String>> = ArrayList(files.size + 1)
        for (file in files) {
            val rawContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8)
            val beginIndex = rawContent.indexOf("---")
            val endIdx = rawContent.indexOf("---", 3)
            val metaDataStr = rawContent.substring(beginIndex + 3, endIdx)
            val content = rawContent.substring(endIdx + 3)
            val metaData = YAML_MAPPER.readValue(metaDataStr, RawMetaData::class.java)
            parseResult.add(Pair(metaData, content))
        }
        return parseResult
    }
}
