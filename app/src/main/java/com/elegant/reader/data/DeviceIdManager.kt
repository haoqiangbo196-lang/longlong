package com.elegant.reader.data

import android.content.Context
import java.util.UUID

/**
 * 设备唯一标识：首次启动生成 UUID 并持久化。
 * 用途：求书功能的匿名用户标识（不登录、不注册，但能区分不同设备）。
 */
object DeviceIdManager {

    private const val PREFS = "device_prefs"
    private const val KEY_DEVICE_ID = "device_id"

    @Volatile
    private var cached: String? = null

    fun getDeviceId(context: Context): String {
        cached?.let { return it }
        val prefs = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getString(KEY_DEVICE_ID, null)?.let {
            cached = it
            return it
        }
        val newId = UUID.randomUUID().toString().replace("-", "")
        prefs.edit().putString(KEY_DEVICE_ID, newId).apply()
        cached = newId
        return newId
    }
}
