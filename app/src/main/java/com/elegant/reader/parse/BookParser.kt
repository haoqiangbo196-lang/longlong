package com.elegant.reader.parse

import java.io.File

/**
 * 书籍解析器统一入口，根据文件类型分派到具体解析器。
 */
object BookParser {

    fun parse(file: File): ParsedBook {
        return when (file.extension.lowercase()) {
            "txt" -> TxtParser.parse(file)
            "epub" -> EpubParser.parse(file)
            else -> throw IllegalArgumentException("不支持的文件格式: ${file.extension}")
        }
    }
}
