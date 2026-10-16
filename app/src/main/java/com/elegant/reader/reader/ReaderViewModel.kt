package com.elegant.reader.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elegant.reader.ReaderApplication
import com.elegant.reader.data.Book
import com.elegant.reader.parse.Chapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 阅读器 ViewModel，管理章节加载、分页、翻页状态与进度保存。
 */
class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ReaderApplication
    private val repository = app.bookRepository
    private val contentRepository = ReaderContentRepository(application)

    // 排版设置
    private val _settings = MutableStateFlow(ReaderSettings())
    val settings: StateFlow<ReaderSettings> = _settings.asStateFlow()

    // 当前书籍
    private val _book = MutableStateFlow<Book?>(null)
    val book: StateFlow<Book?> = _book.asStateFlow()

    // 章节列表
    private val _chapters = MutableStateFlow<List<Chapter>>(emptyList())
    val chapters: StateFlow<List<Chapter>> = _chapters.asStateFlow()

    // 当前章节索引
    private val _currentChapterIndex = MutableStateFlow(0)
    val currentChapterIndex: StateFlow<Int> = _currentChapterIndex.asStateFlow()

    // 当前章节分页结果
    private val _pages = MutableStateFlow<List<Page>>(emptyList())
    val pages: StateFlow<List<Page>> = _pages.asStateFlow()

    // 当前页码
    private val _currentPageIndex = MutableStateFlow(0)
    val currentPageIndex: StateFlow<Int> = _currentPageIndex.asStateFlow()

    // 加载状态
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val paginator = Paginator()

    private var pageWidthPx = 0
    private var pageHeightPx = 0
    private var density = 1f

    private var readingStartTime = System.currentTimeMillis()

    /**
     * 初始化阅读器。
     */
    fun init(bookId: Long) {
        viewModelScope.launch {
            val book = repository.getBook(bookId) ?: return@launch
            _book.value = book
            _currentChapterIndex.value = book.currentChapterIndex
            loadChapters(book)
        }
    }

    private suspend fun loadChapters(book: Book) {
        _loading.value = true
        val chapters = contentRepository.loadChapters(book.filePath)
        _chapters.value = chapters
        _loading.value = false
        repaginate()
    }

    /**
     * 更新排版区域尺寸并重新分页。
     */
    fun updatePageSize(widthPx: Int, heightPx: Int, density: Float) {
        this.pageWidthPx = widthPx
        this.pageHeightPx = heightPx
        this.density = density
        repaginate()
    }

    /**
     * 应用新的排版设置并重新分页。
     */
    fun updateSettings(newSettings: ReaderSettings) {
        _settings.value = newSettings
        repaginate()
    }

    private fun repaginate() {
        val chapters = _chapters.value
        if (chapters.isEmpty() || pageWidthPx <= 0 || pageHeightPx <= 0) return
        val chapter = chapters.getOrNull(_currentChapterIndex.value) ?: return
        val pages = paginator.paginate(
            text = chapter.content,
            settings = _settings.value,
            pageWidthPx = pageWidthPx,
            pageHeightPx = pageHeightPx,
            density = density
        )
        _pages.value = pages
        _currentPageIndex.value = 0
    }

    /**
     * 跳转到指定章节。
     */
    fun gotoChapter(index: Int) {
        if (index !in _chapters.value.indices) return
        _currentChapterIndex.value = index
        repaginate()
    }

    fun nextPage() {
        val pages = _pages.value
        if (pages.isEmpty()) return
        if (_currentPageIndex.value < pages.size - 1) {
            _currentPageIndex.value = _currentPageIndex.value + 1
        } else {
            gotoChapter(_currentChapterIndex.value + 1)
        }
        saveProgress()
    }

    fun prevPage() {
        if (_currentPageIndex.value > 0) {
            _currentPageIndex.value = _currentPageIndex.value - 1
        } else {
            gotoChapter(_currentChapterIndex.value - 1)
        }
        saveProgress()
    }

    /**
     * 保存阅读进度。
     */
    fun saveProgress() {
        val book = _book.value ?: return
        val chapters = _chapters.value
        val pages = _pages.value
        if (chapters.isEmpty()) return

        val elapsed = System.currentTimeMillis() - readingStartTime
        readingStartTime = System.currentTimeMillis()

        // 计算整体进度
        val chapterIndex = _currentChapterIndex.value
        val totalChars = chapters.sumOf { it.content.length }
        var charOffset = 0
        for (i in 0 until chapterIndex) {
            charOffset += chapters[i].content.length
        }
        val pageStart = pages.getOrNull(_currentPageIndex.value)?.startOffset ?: 0
        charOffset += pageStart
        val progress = if (totalChars > 0) charOffset.toFloat() / totalChars else 0f

        viewModelScope.launch {
            repository.updateProgress(
                bookId = book.id,
                chapterIndex = chapterIndex,
                charOffset = pageStart,
                progress = progress,
                readingTimeDelta = elapsed
            )
        }
    }

    /**
     * 当前页文本。
     */
    fun currentPageText(): String {
        return _pages.value.getOrNull(_currentPageIndex.value)?.text ?: ""
    }

    override fun onCleared() {
        super.onCleared()
        saveProgress()
    }
}
