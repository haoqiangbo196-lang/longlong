package com.elegant.reader.ui.reader

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.elegant.reader.reader.Page

/**
 * 基于 Canvas 的文字渲染组件。
 *
 * 参考 Legado 的 ContentTextView + TextLine.draw 架构：
 * 使用 Android 原生 Canvas.drawText 逐行绘制分页好的文本，
 * 系统默认字体，保证与分页测量完全一致。
 */
@Composable
fun TextPageCanvas(
    page: Page,
    textColor: Color,
    textSizePx: Float,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextPageView(context).apply {
                this.textColor = textColor.toArgb()
                this.textSizePx = textSizePx
                this.page = page
            }
        },
        update = { view ->
            view.textColor = textColor.toArgb()
            view.textSizePx = textSizePx
            view.page = page
        }
    )
}

/**
 * 自定义 View：负责逐行绘制文本。
 */
private class TextPageView(context: Context) : View(context) {

    var textColor: Int = 0xFF000000.toInt()
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    var textSizePx: Float = 18f * 3f
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    var page: Page = Page(0, emptyList())
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = textColor
        paint.textSize = textSizePx

        for (line in page.lines) {
            // 去除行尾换行符，避免绘制出空白/换行标记
            val text = line.text.replace("\n", "").replace("\r", "")
            if (text.isEmpty()) continue
            canvas.drawText(text, 0f, line.baseline, paint)
        }
    }
}
