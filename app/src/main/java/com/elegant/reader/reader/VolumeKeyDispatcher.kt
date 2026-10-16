package com.elegant.reader.reader

import android.view.KeyEvent

/**
 * 音量键翻页分发器（单例桥接）。
 *
 * MainActivity 重写 dispatchKeyEvent 拦截音量键后，通过本单例转发给
 * 当前活跃的阅读器，实现音量键上下翻页。
 */
object VolumeKeyDispatcher {

    /**
     * 当前阅读器的翻页回调。
     * 进入阅读器时注册，退出时清空。
     */
    @Volatile
    var onVolumeDown: (() -> Unit)? = null
    @Volatile
    var onVolumeUp: (() -> Unit)? = null

    /**
     * 处理音量键事件。
     * @return true 表示已消费该事件（阻止系统默认音量调节）
     */
    fun dispatch(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                onVolumeDown?.invoke() != null
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                onVolumeUp?.invoke() != null
            }
            else -> false
        }
    }

    fun clear() {
        onVolumeDown = null
        onVolumeUp = null
    }
}
