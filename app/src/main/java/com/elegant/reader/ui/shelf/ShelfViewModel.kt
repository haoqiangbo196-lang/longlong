package com.elegant.reader.ui.shelf

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elegant.reader.ReaderApplication
import com.elegant.reader.data.Book
import com.elegant.reader.data.BookImporter
import com.elegant.reader.data.BookRequest
import com.elegant.reader.data.BookRequestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 书架 ViewModel。
 */
class ShelfViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ReaderApplication
    private val repository = app.bookRepository
    private val importer = BookImporter(application, repository)
    private val requestRepository = BookRequestRepository(application)

    val books: StateFlow<List<Book>> = repository.observeAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _importing = MutableStateFlow(false)
    val importing: StateFlow<Boolean> = _importing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ---- 求书功能 ----

    /** 我的求书记录 */
    private val _requests = MutableStateFlow<List<BookRequest>>(emptyList())
    val requests: StateFlow<List<BookRequest>> = _requests.asStateFlow()

    /** 求书提交中 */
    private val _requestSubmitting = MutableStateFlow(false)
    val requestSubmitting: StateFlow<Boolean> = _requestSubmitting.asStateFlow()

    /** 求书操作提示（成功/失败） */
    private val _requestMessage = MutableStateFlow<String?>(null)
    val requestMessage: StateFlow<String?> = _requestMessage.asStateFlow()

    /** 下载中的记录 objectId */
    private val _downloadingId = MutableStateFlow<String?>(null)
    val downloadingId: StateFlow<String?> = _downloadingId.asStateFlow()

    /**
     * 提交求书。
     */
    fun submitRequest(bookTitle: String, note: String) {
        if (bookTitle.isBlank()) {
            _requestMessage.value = "请填写书名"
            return
        }
        viewModelScope.launch {
            _requestSubmitting.value = true
            try {
                val (ok, msg) = requestRepository.submitAwait(bookTitle.trim(), note.trim())
                _requestMessage.value = msg
                if (ok) refreshRequests()
            } catch (e: Exception) {
                _requestMessage.value = e.message ?: "提交失败"
            } finally {
                _requestSubmitting.value = false
            }
        }
    }

    /**
     * 刷新我的求书记录。
     */
    fun refreshRequests() {
        viewModelScope.launch {
            try {
                val (list, err) = requestRepository.queryMineAwait()
                if (err == null) _requests.value = list
                else _requestMessage.value = err
            } catch (e: Exception) {
                // 静默失败：求书记录查询失败不影响主流程
            }
        }
    }

    /**
     * 下载已提供的书并导入书架。
     */
    fun downloadRequestBook(request: BookRequest) {
        if (request.bookFileUrl.isBlank()) {
            _requestMessage.value = "该记录暂无文件"
            return
        }
        viewModelScope.launch {
            _downloadingId.value = request.objectId
            try {
                val file = requestRepository.downloadBook(request)
                importer.importFromDownloadedFile(file, request.bookFileName.ifBlank { request.bookTitle })
                _requestMessage.value = "《${request.bookTitle}》已加入书架"
            } catch (e: Exception) {
                _requestMessage.value = e.message ?: "下载失败"
            } finally {
                _downloadingId.value = null
            }
        }
    }

    fun clearRequestMessage() {
        _requestMessage.value = null
    }

    /**
     * 导入书籍。
     */
    fun importBook(uri: Uri) {
        viewModelScope.launch {
            _importing.value = true
            _error.value = null
            try {
                importer.importFromUri(uri)
            } catch (e: Exception) {
                _error.value = e.message ?: "导入失败"
            } finally {
                _importing.value = false
            }
        }
    }

    /**
     * 删除书籍。
     */
    fun deleteBook(book: Book) {
        viewModelScope.launch {
            repository.deleteBook(book)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
