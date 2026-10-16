package com.elegant.reader.parse

import java.io.File
import java.nio.charset.Charset

/**
 * TXT 解析器：编码检测 + 章节智能识别。
 */
object TxtParser {

    fun parse(file: File): ParsedBook {
        val bytes = file.readBytes()
        val charset = detectCharset(bytes)
        val raw = String(bytes, charset)

        val normalized = raw
            .replace("\r\n", "\n")
            .replace("\r", "\n")

        val title = file.nameWithoutExtension

        val chapters = splitChapters(normalized)

        return ParsedBook(
            title = title,
            author = "",
            chapters = chapters
        )
    }

    /**
     * 检测文本编码：优先 UTF-8，其次 GBK/GB18030，最后 UTF-16。
     */
    fun detectCharset(bytes: ByteArray): Charset {
        // BOM 检测
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()
        ) {
            return Charsets.UTF_8
        }
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return Charsets.UTF_16LE
        }
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return Charsets.UTF_16BE
        }

        // 尝试严格 UTF-8 解码
        return try {
            val decoder = Charsets.UTF_8.newDecoder()
            decoder.decode(java.nio.ByteBuffer.wrap(bytes))
            Charsets.UTF_8
        } catch (e: Exception) {
            // 回退到 GBK
            runCatching { Charset.forName("GB18030") }.getOrElse { Charsets.UTF_8 }
        }
    }

    /**
     * 智能章节切分：识别「第X章 / 第X节 / 第X回 / Chapter X / 序言 / 楔子 / 尾声」等模式。
     */
    private fun splitChapters(text: String): List<Chapter> {
        val lines = text.split("\n")

        // 章节标题正则：中文序号 + 英文序号 + 常见结构词
        val chapterRegex = Regex(
            pattern = "^\\s*((第\\s*[0-9一二三四五六七八九十百千万零两]+\\s*[章节回卷部篇集])" +
                    "|(Chapter\\s+\\d+)|(卷[一二三四五六七八九十百千万零两]+)" +
                    "|([序楔终尾]章)|(序言|楔子|尾声|番外|后记|附录|前言|引子))\\s*[:：]?\\s*.*$",
            option = RegexOption.IGNORE_CASE
        )

        val chapters = mutableListOf<Chapter>()
        val contentBuffer = StringBuilder()
        var currentTitle: String? = null

        fun flush() {
            val body = contentBuffer.toString().trim()
            if (body.isNotEmpty() || currentTitle != null) {
                val title = currentTitle ?: "正文"
                chapters.add(Chapter(title, body))
            }
            contentBuffer.clear()
            currentTitle = null
        }

        for (line in lines) {
            val trimmed = line.trim()
            val match = chapterRegex.matchEntire(trimmed)
            if (match != null && trimmed.length <= 40) {
                // 新章节开始
                flush()
                currentTitle = trimmed
            } else {
                contentBuffer.append(line).append('\n')
            }
        }
        flush()

        // 如果没有识别到章节，整本书作为一章
        if (chapters.isEmpty()) {
            return listOf(Chapter("正文", text.trim()))
        }

        return chapters
    }
}
