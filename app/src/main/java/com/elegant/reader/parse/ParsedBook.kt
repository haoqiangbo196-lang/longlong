package com.elegant.reader.parse

/**
 * 章节模型，表示一本书中的一章。
 *
 * @param title 章节标题
 * @param content 章节正文纯文本
 */
data class Chapter(
    val title: String,
    val content: String
)

/**
 * 解析后的书籍模型。
 */
data class ParsedBook(
    val title: String,
    val author: String,
    val chapters: List<Chapter>,
    val coverPath: String? = null
) {
    val totalChars: Int get() = chapters.sumOf { it.content.length }
}
