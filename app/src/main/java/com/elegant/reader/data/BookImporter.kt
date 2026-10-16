package com.elegant.reader.data

import android.content.Context
import android.net.Uri
import com.elegant.reader.parse.BookParser
import com.elegant.reader.parse.ParsedBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 书籍导入服务：拷贝文件到应用私有目录、解析并写入数据库。
 */
class BookImporter(
    private val context: Context,
    private val repository: BookRepository
) {

    private val booksDir: File
        get() = File(context.filesDir, "books").apply { mkdirs() }

    /**
     * 从 Uri 导入书籍。
     */
    suspend fun importFromUri(uri: Uri): Book = withContext(Dispatchers.IO) {
        val fileName = queryFileName(uri) ?: "book_${System.currentTimeMillis()}"
        val extension = fileName.substringAfterLast('.', "").lowercase()

        if (extension !in setOf("txt", "epub")) {
            throw IllegalArgumentException("仅支持 TXT 与 EPUB 格式")
        }

        // 拷贝到私有目录
        val destFile = File(booksDir, "${System.currentTimeMillis()}_$fileName")
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IllegalStateException("无法读取文件")

        // 解析（传入原始文件名用于提取书名，避免时间戳前缀混入书名）
        val parsed: ParsedBook = BookParser.parse(destFile, fileName)

        // 生成封面（暂用空，后续可用 epub 封面）
        val book = Book(
            title = parsed.title.ifBlank { fileName.substringBeforeLast('.') },
            author = parsed.author,
            filePath = destFile.absolutePath,
            fileType = extension,
            totalWords = parsed.totalChars.toLong(),
            chapterCount = parsed.chapters.size,
            coverPath = parsed.coverPath
        )

        val id = repository.addBook(book)
        book.copy(id = id)
    }

    private fun queryFileName(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    return it.getString(nameIndex)
                }
            }
        }
        return null
    }
}
