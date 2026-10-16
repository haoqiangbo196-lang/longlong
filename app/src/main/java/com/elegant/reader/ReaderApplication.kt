package com.elegant.reader

import android.app.Application
import com.elegant.reader.data.AppDatabase
import com.elegant.reader.data.BookRepository

class ReaderApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val bookRepository by lazy { BookRepository(database.bookDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}
