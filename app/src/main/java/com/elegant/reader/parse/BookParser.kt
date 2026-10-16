package com.elegant.reader.parse

import java.io.File

/**
 * 书籍解析器统一入口，根据文件类型分派到具体解析器。
 */
object BookParser {

    /**
     * 解析书籍。
     *
     * @param file 书籍文件（可能是带时间戳前缀的临时文件）
     * @param displayName 原始文件名（用于提取书名，不含时间戳前缀）
     */
    fun parse(file: File, displayName: String? = null): ParsedBook {
        return when (file.extension.lowercase()) {
            "txt" -> TxtParser.parse(file, displayName)
            "epub" -> EpubParser.parse(file)
            else -> throw IllegalArgumentException("不支持的文件格式: ${file.extension}")
        }
    }
}
