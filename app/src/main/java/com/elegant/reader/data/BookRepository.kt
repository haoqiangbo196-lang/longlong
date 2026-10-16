package com.elegant.reader.data

import kotlinx.coroutines.flow.Flow

/**
 * 书籍仓库，统一封装数据访问。
 */
class BookRepository(private val dao: BookDao) {

    fun observeAllBooks(): Flow<List<Book>> = dao.observeAll()

    suspend fun getBook(id: Long): Book? = dao.getById(id)

    suspend fun addBook(book: Book): Long = dao.insert(book)

    suspend fun updateBook(book: Book) = dao.update(book)

    suspend fun deleteBook(book: Book) = dao.delete(book)

    suspend fun deleteBook(id: Long) = dao.deleteById(id)

    /**
     * 更新阅读进度。
     */
    suspend fun updateProgress(
        bookId: Long,
        chapterIndex: Int,
        charOffset: Int,
        progress: Float,
        readingTimeDelta: Long
    ) {
        val book = dao.getById(bookId) ?: return
        dao.update(
            book.copy(
                currentChapterIndex = chapterIndex,
                currentCharOffset = charOffset,
                progress = progress,
                totalReadingTime = book.totalReadingTime + readingTimeDelta,
                lastReadTime = System.currentTimeMillis()
            )
        )
    }
}
