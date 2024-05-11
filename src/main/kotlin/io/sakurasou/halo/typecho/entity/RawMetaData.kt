package io.sakurasou.halo.typecho.entity

import java.time.LocalDateTime

/**
 * @author mashirot
 * 2024/5/6 23:08
 */
data class RawMetaData(
    val layout: String?, val cid: Long?, val title: String?, val slug: String?, val date: LocalDateTime,
    val updated: LocalDateTime?, val status: String?, val author: String?, val categories: List<String>?,
    val tags: List<String>?, val customSummary: String?, val mathjax: String?,
    val noThumbInfoEmoji: String?, val noThumbInfoStyle: String?, val outdatedNotice: String?,
    val parseWay: String?, val reprint: String?, val thumb: String?, val thumbChoice: String?,
    val thumbDesc: String?, val thumbSmall: String?, val thumbStyle: String?
)
