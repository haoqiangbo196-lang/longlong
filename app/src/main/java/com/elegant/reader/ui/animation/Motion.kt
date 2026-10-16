package com.elegant.reader.ui.animation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * 非线性动画缓动曲线集合。
 * 所有界面过渡、翻页、菜单弹出均使用这些缓动，保证丝滑自然。
 */
object Motion {

    /** 标准进出场：快出慢进，用于大多数界面过渡 */
    val Standard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /** 强调动画：用于弹窗、卡片入场 */
    val Emphasized: Easing = CubicBezierEasing(0.3f, 0.0f, 0.1f, 1.0f)

    /** 减速动画：用于元素退场 */
    val Decelerate: Easing = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)

    /** 加速动画：用于元素进场 */
    val Accelerate: Easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)

    /** 弹性回弹，用于阅读器菜单、设置面板 */
    val Bouncy: Easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1.0f)

    /** 翻页仿真用的缓动 */
    val PageTurn: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)

    /** 弹簧动画参数（无回弹） */
    fun <T> springSmooth(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    /** 弹簧动画参数（轻微回弹） */
    fun <T> springBouncy(): SpringSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
