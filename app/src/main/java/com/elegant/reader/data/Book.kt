package com.elegant.reader.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 书籍实体，存储书架上的书籍元数据。
 */
@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 书籍标题 */
    val title: String,
    /** 作者 */
    val author: String = "",
    /** 原始文件在应用私有目录中的路径 */
    val filePath: String,
    /** 文件类型：txt / epub */
    val fileType: String,
    /** 封面图片路径（epub 内提取或生成的） */
    val coverPath: String? = null,
    /** 书籍总字数 */
    val totalWords: Long = 0,
    /** 章节数量 */
    val chapterCount: Int = 0,
    /** 当前阅读章节索引 */
    val currentChapterIndex: Int = 0,
    /** 当前章节内的字符偏移 */
    val currentCharOffset: Int = 0,
    /** 阅读进度 0f..1f */
    val progress: Float = 0f,
    /** 累计阅读时长（毫秒） */
    val totalReadingTime: Long = 0,
    /** 最近阅读时间戳 */
    val lastReadTime: Long = 0,
    /** 添加时间戳 */
    val addedTime: Long = System.currentTimeMillis()
)
