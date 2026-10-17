package com.elegant.reader

import android.app.Application
import cn.bmob.v3.Bmob
import com.elegant.reader.data.AppDatabase
import com.elegant.reader.data.BookRepository

class ReaderApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val bookRepository by lazy { BookRepository(database.bookDao()) }

    override fun onCreate() {
        super.onCreate()
        // Bmob 后端云（求书功能）
        Bmob.initialize(this, BMOB_APP_ID)
    }

    companion object {
        /** Bmob Application ID（客户端公开标识） */
        const val BMOB_APP_ID = "f5ef28ae1b94d740e4e617a0bcf0df9f"
    }
}
