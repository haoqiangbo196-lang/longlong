package com.elegant.reader.reader

import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils

/**
 * 文本分页器：将章节内容按可用区域切成若干页。
 *
 * 使用 [StaticLayout] 进行文本测量，天然支持中英文混排、自动换行。
 */
class Paginator {

    /**
     * 将一段文本分页。
     *
     * @param text 章节全文
     * @param settings 排版设置
     * @param pageWidthPx 页面可用宽度（像素）
     * @param pageHeightPx 页面可用高度（像素）
     * @param density 屏幕密度，用于 sp/dp 转换
     * @return 每页的起始字符偏移列表，以及对应页面文本
     */
    fun paginate(
        text: String,
        settings: ReaderSettings,
        pageWidthPx: Int,
        pageHeightPx: Int,
        density: Float
    ): List<Page> {
        if (text.isEmpty() || pageWidthPx <= 0 || pageHeightPx <= 0) {
            return listOf(Page(0, text))
        }

        // 段间距：在段落之间插入空行，使段落分隔在渲染和分页中一致
        val processedText = applyParagraphSpacing(text, settings)

        val paint = createTextPaint(settings, density)
        val pages = mutableListOf<Page>()

        var currentStart = 0
        val totalLength = processedText.length

        while (currentStart < totalLength) {
            val layout = StaticLayout.Builder
                .obtain(processedText, currentStart, totalLength, paint, pageWidthPx)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1f)
                .setIncludePad(true)
                .build()

            // 找到能放入页面的最后一行
            var lineCount = layout.lineCount
            var end = totalLength

            if (layout.height > pageHeightPx) {
                lineCount = layout.getLineForVertical(pageHeightPx)
                if (lineCount <= 0) lineCount = 1
                end = layout.getLineStart(lineCount)
                // 避免死循环：确保每页至少前进一步
                if (end <= currentStart) {
                    end = layout.getLineEnd(0)
                }
            }

            val pageText = processedText.substring(currentStart, end)
            pages.add(Page(currentStart, pageText))
            currentStart = end
        }

        // 如果文本有内容但没有产生页（极端情况），兜底一页
        if (pages.isEmpty()) {
            pages.add(Page(0, processedText))
        }

        return pages
    }

    /**
     * 根据段间距设置，在段落之间插入空行。
     * 段落间距 0~30 映射为 0~3 个空行。
     */
    private fun applyParagraphSpacing(text: String, settings: ReaderSettings): String {
        if (settings.paragraphSpacing <= 0f) return text
        val blankLines = (settings.paragraphSpacing / 10f).toInt().coerceIn(0, 3)
        if (blankLines == 0) return text

        // 将段落边界（\n）替换为「\n + 空行」，保留原有换行
        val separator = "\n" + "\n".repeat(blankLines)
        // 只对单个换行符处理，避免重复的空行继续叠加
        return text
            .replace(Regex("\n{2,}"), "\n")  // 先压缩连续换行为单个
            .replace("\n", separator)          // 每个换行后插入空行
    }

    private fun createTextPaint(settings: ReaderSettings, density: Float): TextPaint {
        val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
        paint.textSize = settings.fontSizeSp * density
        paint.color = 0xFF000000.toInt()
        paint.typeface = when (settings.fontFamily) {
            FontFamilyOption.DEFAULT -> Typeface.DEFAULT
            FontFamilyOption.SERIF -> Typeface.SERIF
            FontFamilyOption.SANS -> Typeface.SANS_SERIF
            FontFamilyOption.MONO -> Typeface.MONOSPACE
        }
        return paint
    }
}

/**
 * 一页文本，包含起始字符偏移和页面内容。
 */
data class Page(
    val startOffset: Int,
    val text: String
)
