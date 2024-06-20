package io.sakurasou.halo.typecho.entity

import java.time.LocalDateTime

/**
 * @author mashirot
 * 2024/5/6 23:08
 */
class RawMetaData {
    val layout: String?
    val cid: Long?
    val title: String?
    val slug: String?
    val date: LocalDateTime
    val updated: LocalDateTime?
    val status: String?
    val author: String?
    val categories: List<String>?
    val tags: List<String>?
    val customSummary: String?

    constructor(results: Map<*, *>) {
        layout = results["layout"] as String?
        cid = results["cid"] as Long?
        title = results["title"] as String?
        slug = results["slug"] as String?
        date = LocalDateTime.parse(results["date"] as String)
        updated = results["updated"]?.let { LocalDateTime.parse(it as String) }
        status = results["status"] as String?
        author = results["author"] as String?
        categories = results["categories"] as List<String>?
        tags = results["tags"] as List<String>?
        customSummary = results["customSummary"] as String?
    }
}
