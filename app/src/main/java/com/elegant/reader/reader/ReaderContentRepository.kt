package com.elegant.reader.reader

import android.content.Context
import com.elegant.reader.parse.BookParser
import com.elegant.reader.parse.Chapter
import java.io.File

/**
 * 阅读内容仓库：加载书籍章节，供阅读器使用。
 */
class ReaderContentRepository(private val context: Context) {

    @Volatile
    private var chapterCache: MutableMap<Long, List<Chapter>> = mutableMapOf()

    /**
     * 加载书籍的全部章节。
     */
    suspend fun loadChapters(filePath: String): List<Chapter> {
        val file = File(filePath)
        val parsed = BookParser.parse(file)
        val key = filePath.hashCode().toLong()
        chapterCache[key] = parsed.chapters
        return parsed.chapters
    }

    /**
     * 获取单章内容。
     */
    suspend fun getChapter(filePath: String, chapterIndex: Int): Chapter? {
        val file = File(filePath)
        val chapters = chapterCache[filePath.hashCode().toLong()]
            ?: loadChapters(filePath)
        return chapters.getOrNull(chapterIndex)
    }
}
