package com.elegant.reader.data

import android.content.Context
import cn.bmob.v3.BmobQuery
import cn.bmob.v3.exception.BmobException
import cn.bmob.v3.listener.FindListener
import cn.bmob.v3.listener.SaveListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * 求书功能仓库：提交求书、查询我的求书、下载书文件。
 */
class BookRequestRepository(private val context: Context) {

    private val deviceId: String by lazy { DeviceIdManager.getDeviceId(context) }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 提交求书登记。
     */
    fun submit(bookTitle: String, note: String, callback: (Boolean, String) -> Unit) {
        val request = BookRequest().apply {
            this.deviceId = this@BookRequestRepository.deviceId
            this.bookTitle = bookTitle
            this.note = note
            this.status = "pending"
        }
        request.save(object : SaveListener() {
            override fun done(objectId: String?, e: BmobException?) {
                if (e == null) {
                    callback(true, "登记成功，找到书会送到你的书架")
                } else {
                    callback(false, e.message ?: "提交失败，请检查网络")
                }
            }
        })
    }

    /**
     * 查询本设备的求书记录（按提交时间倒序）。
     */
    fun queryMine(callback: (List<BookRequest>, String?) -> Unit) {
        val query = BmobQuery<BookRequest>()
        query.addWhereEqualTo("deviceId", deviceId)
        query.setLimit(50)
        query.order("-createdAt")
        query.findObjects(object : FindListener<BookRequest>() {
            override fun done(list: MutableList<BookRequest>?, e: BmobException?) {
                if (e == null) {
                    callback(list ?: emptyList(), null)
                } else {
                    callback(emptyList(), e.message ?: "查询失败")
                }
            }
        })
    }

    /**
     * 下载已提供的书文件到应用私有目录，返回下载后的文件。
     */
    suspend fun downloadBook(request: BookRequest): File = withContext(Dispatchers.IO) {
        val url = request.bookFileUrl
        require(url.isNotBlank()) { "该记录暂无文件地址" }

        val dir = File(context.filesDir, "requests").apply { mkdirs() }
        val rawName = request.bookFileName.ifBlank { "${request.bookTitle}.txt" }
        val safeName = rawName.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        val dest = File(dir, "${System.currentTimeMillis()}_$safeName")

        val req = Request.Builder().url(url).build()
        httpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw IllegalStateException("下载失败 (HTTP ${resp.code})")
            val body = resp.body ?: throw IllegalStateException("下载内容为空")
            body.byteStream().use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
        }
        dest
    }

    /**
     * 挂起式提交（供协程调用）。
     */
    suspend fun submitAwait(bookTitle: String, note: String): Pair<Boolean, String> =
        suspendCancellableCoroutine { cont ->
            submit(bookTitle, note) { ok, msg -> cont.resume(ok to msg) }
        }

    /**
     * 挂起式查询（供协程调用）。
     */
    suspend fun queryMineAwait(): Pair<List<BookRequest>, String?> =
        suspendCancellableCoroutine { cont ->
            queryMine { list, err -> cont.resume(list to err) }
        }
}
