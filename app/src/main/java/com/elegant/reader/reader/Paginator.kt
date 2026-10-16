package com.elegant.reader.reader

import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

/**
 * 文本分页器。
 *
 * 核心思想（参考 Legado 成熟引擎）：
 * 分页与渲染共用同一套 [TextPaint] + [StaticLayout] 测量体系，
 * 精确计算出每一行的文本、基线 Y 坐标与行高，保证「排版测量」与「最终显示」完全一致。
 *
 * 字体（Typeface.createFromFile）、额外行距（lineSpacingExtra）、
 * 段距（paragraphSpacing 像素）都在测量阶段生效，从根本上解决
 * 「分页一套、渲染又一套」导致的字体/行距/段距失效问题。
 */
class Paginator {

    /**
     * 将章节文本分页。
     *
     * @param text 章节全文
     * @param settings 排版设置
     * @param pageWidthPx 页面可用宽度（像素）
     * @param pageHeightPx 页面可用高度（像素）
     * @param density 屏幕密度
     * @param typeface 正文使用的字体（Typeface.createFromFile 加载），为空则用默认字体
     * @return 页面列表，每页包含若干行（文本 + 坐标）
     */
    fun paginate(
        text: String,
        settings: ReaderSettings,
        pageWidthPx: Int,
        pageHeightPx: Int,
        density: Float,
        typeface: Typeface? = null
    ): List<Page> {
        if (text.isEmpty()) {
            return listOf(Page(0, emptyList()))
        }
        if (pageWidthPx <= 0 || pageHeightPx <= 0) {
            return listOf(Page(0, emptyList()))
        }

        val paint = createTextPaint(settings, density, typeface)

        // 额外行距：Legado 中 lineSpacingExtra 由 sp 值 / 10 得到像素
        val lineSpacingExtraPx = settings.lineSpacingExtraSp / 10f * density
        // 段距：直接使用像素值
        val paragraphSpacingPx = settings.paragraphSpacing * density

        val pages = mutableListOf<Page>()
        var start = 0
        val totalLength = text.length

        while (start < totalLength) {
            val layout = StaticLayout.Builder
                .obtain(text, start, totalLength, paint, pageWidthPx)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(lineSpacingExtraPx, 1f)
                .setIncludePad(false)
                .build()

            val lines = mutableListOf<TextLineData>()
            var currentY = 0f
            var lastLineEnd = start
            var broken = false
            val maxY = pageHeightPx.toFloat()

            for (i in 0 until layout.lineCount) {
                val lineStart = layout.getLineStart(i)
                val lineEnd = layout.getLineEnd(i)
                val lineText = text.substring(lineStart, lineEnd)
                val baseline = layout.getLineBaseline(i).toFloat()
                val top = layout.getLineTop(i).toFloat()

                // 段距：上一行是段落结尾（以 \n 结束）时，额外增加段距
                val lineStr = lineText
                val isParagraphEnd = lineStr.endsWith("\n") || lineStr.endsWith("\r")

                // 判断是否放得下这一行
                val lineBottom = top + (layout.getLineBottom(i) - layout.getLineTop(i))
                val lineHeight = lineBottom - top
                val requiredBottom = top + lineHeight + lineSpacingExtraPx

                // 加上段距后仍放不下，则换页
                val extraParagraphSpace = if (lines.isNotEmpty() && lines.last().isParagraphEnd) {
                    paragraphSpacingPx
                } else {
                    0f
                }

                if (currentY + lineHeight + extraParagraphSpace > maxY && lines.isNotEmpty()) {
                    // 当前行放不下，截断到上一行
                    broken = true
                    break
                }

                // 累加段距到 currentY
                currentY += extraParagraphSpace

                lines.add(
                    TextLineData(
                        text = lineText,
                        top = currentY,
                        baseline = currentY + (baseline - top),
                        height = lineHeight,
                        isParagraphEnd = isParagraphEnd
                    )
                )
                currentY += lineHeight
                lastLineEnd = lineEnd
            }

            if (lines.isEmpty()) {
                // 极端情况：单行都放不下，至少保留一行
                val firstLineEnd = layout.getLineEnd(0)
                val lineText = text.substring(start, firstLineEnd)
                lines.add(
                    TextLineData(
                        text = lineText,
                        top = 0f,
                        baseline = layout.getLineBaseline(0).toFloat() - layout.getLineTop(0).toFloat(),
                        height = (layout.getLineBottom(0) - layout.getLineTop(0)).toFloat(),
                        isParagraphEnd = lineText.endsWith("\n")
                    )
                )
                lastLineEnd = firstLineEnd
            }

            pages.add(Page(start, lines))

            // 推进
            if (broken) {
                start = layout.getLineStart(lines.size)
            } else {
                start = lastLineEnd
            }

            // 防止死循环
            if (start <= 0 || start == pages.last().startOffset) {
                break
            }
        }

        if (pages.isEmpty()) {
            pages.add(Page(0, emptyList()))
        }
        return pages
    }

    /**
     * 根据设置构建正文画笔（与渲染侧完全一致）。
     */
    fun createTextPaint(
        settings: ReaderSettings,
        density: Float,
        typeface: Typeface? = null
    ): TextPaint {
        val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG)
        paint.textSize = settings.fontSizeSp * density
        paint.typeface = typeface ?: when (settings.fontFamily) {
            FontFamilyOption.DEFAULT -> Typeface.DEFAULT
            FontFamilyOption.SERIF -> Typeface.SERIF
            FontFamilyOption.SANS -> Typeface.SANS_SERIF
            FontFamilyOption.MONO -> Typeface.MONOSPACE
        }
        return paint
    }
}

/**
 * 一行文本的排版数据：文本 + 坐标信息。
 */
data class TextLineData(
    val text: String,
    val top: Float,
    val baseline: Float,
    val height: Float,
    val isParagraphEnd: Boolean = false
)

/**
 * 一页文本，包含起始字符偏移与页面内各行数据。
 */
data class Page(
    val startOffset: Int,
    val lines: List<TextLineData>
)
