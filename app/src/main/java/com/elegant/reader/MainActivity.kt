package com.elegant.reader

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.elegant.reader.reader.VolumeKeyDispatcher
import com.elegant.reader.ui.AppNavigation
import com.elegant.reader.ui.theme.ElegantReaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElegantReaderTheme {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AppNavigation()
                }
            }
        }
    }

    /**
     * 拦截音量键，交给阅读器处理（音量键翻页）。
     * 只有阅读器注册了翻页回调时才会消费事件，否则交给系统默认处理。
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (VolumeKeyDispatcher.dispatch(event.keyCode, event)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}
