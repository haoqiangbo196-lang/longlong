package com.elegant.reader.ui.shelf

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elegant.reader.ReaderApplication
import com.elegant.reader.data.Book
import com.elegant.reader.data.BookImporter
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

    val books: StateFlow<List<Book>> = repository.observeAllBooks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _importing = MutableStateFlow(false)
    val importing: StateFlow<Boolean> = _importing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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
