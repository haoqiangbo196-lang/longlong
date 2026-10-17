package com.elegant.reader.reader

/**
 * 阅读排版设置，控制字号、行距、段距、页边距等。
 *
 * 行距采用「额外行距」模型：
 * lineSpacingExtraSp 表示在行高之外额外增加的行距（以 sp 计），
 * 实际渲染像素 = lineSpacingExtraSp * density，拖动即可见效果。
 */
data class ReaderSettings(
    val fontSizeSp: Float = 18f,
    val lineSpacingExtraSp: Float = 6f,
    val paragraphSpacing: Float = 8f,
    val pageMarginHorizontalDp: Float = 20f,
    val pageMarginTopDp: Float = 16f,
    val pageMarginBottomDp: Float = 16f,
    val theme: ReaderTheme = ReaderTheme.PAPER,
    val pageTurnMode: PageTurnMode = PageTurnMode.SLIDE
)

/**
 * 阅读主题（纸张配色）。
 */
enum class ReaderTheme(
    val background: Long,
    val textColor: Long,
    val displayName: String
) {
    PAPER(0xFFF5EFE0, 0xFF3D3229, "羊皮纸"),
    WHITE(0xFFFFFFFF, 0xFF222222, "纯白"),
    SEPIA(0xFFEFE4CD, 0xFF4A3B2A, "暖黄"),
    GREEN(0xFFCFE3CC, 0xFF2C3E2E, "护眼绿"),
    NIGHT(0xFF1B1B1B, 0xFFB8B8B8, "夜间"),
    DARK(0xFF121212, 0xFF9A9A9A, "暗黑")
}

/**
 * 翻页模式。
 */
enum class PageTurnMode(val displayName: String) {
    SLIDE("左右滑动"),
    SCROLL("上下滚动")
}
