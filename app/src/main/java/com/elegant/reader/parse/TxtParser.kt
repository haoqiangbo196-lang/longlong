package com.elegant.reader.parse

import java.io.File
import java.nio.charset.Charset

/**
 * TXT 解析器：编码检测 + 章节智能识别。
 */
object TxtParser {

    fun parse(file: File, displayName: String? = null): ParsedBook {
        val bytes = file.readBytes()
        val charset = detectCharset(bytes)
        val raw = String(bytes, charset)

        val normalized = raw
            .replace("\r\n", "\n")
            .replace("\r", "\n")

        // 书名优先用传入的原始文件名，否则用文件本身的名字
        val title = (displayName ?: file.name).substringBeforeLast('.', "")

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
            if (isChapterTitle(trimmed)) {
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

    /**
     * 判断一行是否为章节标题。覆盖主流书籍的章节格式。
     */
    private fun isChapterTitle(line: String): Boolean {
        if (line.isEmpty() || line.length > 50) return false

        // 1. 第X章 / 第X节 / 第X回 / 第X卷 / 第X部 / 第X篇 / 第X集 / 第X话
        //    支持中文数字、阿拉伯数字、前导零、空格
        val cnNum = "[0-9０-９一二三四五六七八九十百千万零两〇]+"
        val chapterPattern1 = Regex(
            "^第\\s*$cnNum\\s*[章节回卷部篇集话].{0,40}$"
        )
        if (chapterPattern1.matches(line)) return true

        // 2. Chapter X / CHAPTER X / Chap. X（阿拉伯或罗马数字）
        val chapterPattern2 = Regex(
            "^(?i)(chapter|chap)\\s*\\.?\\s*[0-9IVXLCDM０-９]+.{0,40}$"
        )
        if (chapterPattern2.matches(line)) return true

        // 3. 卷X / 部X / 篇X（中文序号）
        val chapterPattern3 = Regex(
            "^[卷部篇集]\\s*[一二三四五六七八九十百千万零〇]+.{0,40}$"
        )
        if (chapterPattern3.matches(line)) return true

        // 4. 结构词：序章/序言/楔子/尾声/番外/后记/附录/前言/引子/终章/大结局/结局
        val chapterPattern4 = Regex(
            "^(序章|序言|楔子|尾声|终章|大结局|番外|后记|附录|前言|引子|结局).{0,40}$"
        )
        if (chapterPattern4.matches(line)) return true

        // 5. 中文序号开头：一、二、三、……（要求后面有标题文字，避免误判正文里的"一、xxx"）
        val chapterPattern5 = Regex(
            "^[一二三四五六七八九十百千万零〇]{1,6}[、.．].{1,40}$"
        )
        if (chapterPattern5.matches(line)) return true

        // 6. 阿拉伯数字序号：1. / 1、/ 01 / 001 等
        //    要求：序号后必须有标题文字，且序号不能太长（最多4位），避免误判正文编号
        val chapterPattern6 = Regex(
            "^[0-9０-９]{1,4}[、.．]\\s*.{1,40}$"
        )
        if (chapterPattern6.matches(line)) return true

        return false
    }
}
