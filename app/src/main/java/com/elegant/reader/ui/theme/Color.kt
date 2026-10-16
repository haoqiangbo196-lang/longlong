package com.elegant.reader.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 设计系统颜色：锤子 Smartisan OS 拟物风格。
 * 木纹 / 皮革 / 纸张质感，暖棕色调，多层次阴影营造立体浮起感。
 */

// 主色调 - 温润的陶土棕（拟物核心色）
val Terracotta = Color(0xFFB5734D)
val TerracottaDark = Color(0xFF8B5E3C)
val TerracottaLight = Color(0xFFD9A98A)

// 木纹色 - 深胡桃木
val WoodDark = Color(0xFF5C3A28)
val WoodMid = Color(0xFF8A5A3B)
val WoodLight = Color(0xFFA87B54)

// 皮革色
val LeatherBrown = Color(0xFF6B4A35)
val LeatherDark = Color(0xFF4A3322)
val LeatherLight = Color(0xFF8C6A4F)

// 纸张色
val PaperCream = Color(0xFFF5EFE0)
val PaperWhite = Color(0xFFFBF7EF)

// 强调色 - 静谧的黛蓝
val InkBlue = Color(0xFF4A5B6E)
val InkBlueLight = Color(0xFF7C8DA0)

// 背景色 - 温暖米白（拟物桌面背景）
val WarmBackground = Color(0xFFEDE4D3)
val WarmSurface = Color(0xFFF8F2E7)
val WarmSurfaceElevated = Color(0xFFFFFDF8)

// 文字色
val TextPrimary = Color(0xFF3A2E24)
val TextSecondary = Color(0xFF8A7A68)
val TextTertiary = Color(0xFFB3A693)

// 功能色
val SuccessGreen = Color(0xFF6B8E5A)
val WarnAmber = Color(0xFFD9A84E)
val DangerRed = Color(0xFFC05B4D)

// 书架卡片封面渐变（按书籍生成，取自设计稿的书封色系：低饱和、雅致）
val CoverGradients = listOf(
    listOf(Color(0xFF3D5248), Color(0xFF26352D)),  // 墨绿
    listOf(Color(0xFF8FB0CC), Color(0xFF5F87A8)),  // 天蓝
    listOf(Color(0xFF2C3A50), Color(0xFF1A2436)),  // 深蓝
    listOf(Color(0xFFCFC8B8), Color(0xFFA8A091)),  // 米灰
    listOf(Color(0xFF5A7C63), Color(0xFF3D5A46)),  // 翠绿
    listOf(Color(0xFF4A3C33), Color(0xFF2E251F)),  // 深褐
    listOf(Color(0xFF3A4C66), Color(0xFF24324A)),  // 藏蓝
    listOf(Color(0xFF4A6E62), Color(0xFF2F4A40)),  // 青绿
    listOf(Color(0xFF8A7A64), Color(0xFF66584A)),  // 灰褐
    listOf(Color(0xFF6E5A7A), Color(0xFF4A3C55)),  // 黛紫
)

// 主界面配色（取自设计稿）
val PageBg = Color(0xFFFAF4E8)       // 页面背景米色
val CardCream = Color(0xFFFDF9EE)    // 卡片米白
val InkDark = Color(0xFF2A2A26)      // 深墨文字
val InkGray = Color(0xFF8A857A)      // 次要文字
val GreenMain = Color(0xFF6C8977)    // 墨绿主色（按钮/进度条）
val GreenLight = Color(0xFFE8EDE5)   // 进度条底色
val WoodTone = Color(0xFFC9A87A)     // 书封木托
