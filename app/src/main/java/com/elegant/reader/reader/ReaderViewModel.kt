package com.elegant.reader.reader

import android.app.Application
import android.graphics.Typeface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elegant.reader.ReaderApplication
import com.elegant.reader.data.Book
import com.elegant.reader.parse.Chapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    // 当前字体（分页与渲染共用，保证一致）
    private val _typeface = MutableStateFlow<Typeface>(Typeface.DEFAULT)
    val typeface: StateFlow<Typeface> = _typeface.asStateFlow()

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

    // 分页任务，用于取消旧任务避免竞态
    private var paginateJob: Job? = null

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
        val chapters = withContext(Dispatchers.IO) {
            contentRepository.loadChapters(book.filePath)
        }
        _chapters.value = chapters
        _loading.value = false
        repaginate()
    }

    /**
     * 更新排版区域尺寸并重新分页。
     */
    fun updatePageSize(widthPx: Int, heightPx: Int, density: Float) {
        if (this.pageWidthPx == widthPx && this.pageHeightPx == heightPx && this.density == density) {
            return
        }
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
        // 立即更新字体，让渲染侧感知变化
        _typeface.value = FontLoader.load(newSettings)
        repaginate()
    }

    /**
     * 重新分页（异步，避免阻塞主线程导致卡顿）。
     *
     * @param targetPage 分页完成后要定位到的页码（-1 表示定位到最后一页，0 表示第一页）
     */
    private fun repaginate(targetPage: Int = 0) {
        val chapters = _chapters.value
        if (chapters.isEmpty() || pageWidthPx <= 0 || pageHeightPx <= 0) return
        val chapterIndex = _currentChapterIndex.value
        val chapter = chapters.getOrNull(chapterIndex) ?: return
        val settings = _settings.value

        // 取消旧的分页任务，避免竞态
        paginateJob?.cancel()

        paginateJob = viewModelScope.launch {
            val pages = withContext(Dispatchers.Default) {
                val typeface = FontLoader.load(settings)
                paginator.paginate(
                    text = chapter.content,
                    settings = settings,
                    pageWidthPx = pageWidthPx,
                    pageHeightPx = pageHeightPx,
                    density = density,
                    typeface = typeface
                )
            }
            // 分页完成后再回到主线程更新状态
            _pages.value = pages
            _currentPageIndex.value = when {
                targetPage < 0 -> (pages.size - 1).coerceAtLeast(0)
                targetPage >= pages.size -> (pages.size - 1).coerceAtLeast(0)
                else -> targetPage
            }
        }
    }

    /**
     * 跳转到指定章节。
     */
    fun gotoChapter(index: Int) {
        if (index !in _chapters.value.indices) return
        _currentChapterIndex.value = index
        repaginate(targetPage = 0)
    }

    /**
     * 下一页。
     */
    fun nextPage() {
        val pages = _pages.value
        if (pages.isEmpty()) return
        if (_currentPageIndex.value < pages.size - 1) {
            _currentPageIndex.value = _currentPageIndex.value + 1
            saveProgress()
        } else {
            // 章节末尾，跳到下一章第一页
            if (_currentChapterIndex.value < _chapters.value.size - 1) {
                gotoChapter(_currentChapterIndex.value + 1)
                saveProgress()
            }
        }
    }

    /**
     * 上一页。
     */
    fun prevPage() {
        if (_currentPageIndex.value > 0) {
            _currentPageIndex.value = _currentPageIndex.value - 1
            saveProgress()
        } else {
            // 章节开头，跳到上一章最后一页
            if (_currentChapterIndex.value > 0) {
                _currentChapterIndex.value = _currentChapterIndex.value - 1
                repaginate(targetPage = -1)
                saveProgress()
            }
        }
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

    override fun onCleared() {
        super.onCleared()
        saveProgress()
    }
}
