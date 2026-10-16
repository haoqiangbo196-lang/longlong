package com.elegant.reader.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elegant.reader.parse.Chapter
import com.elegant.reader.ui.animation.Motion
import com.elegant.reader.ui.clickableNoIndication

/**
 * 目录抽屉：从左侧滑出，显示章节列表，点击跳转。
 */
@Composable
fun ChapterDrawer(
    visible: Boolean,
    chapters: List<Chapter>,
    currentChapterIndex: Int,
    onChapterClick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // 半透明遮罩
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200, easing = Motion.Standard)),
        exit = fadeOut(tween(200, easing = Motion.Accelerate))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickableNoIndication(onClick = onDismiss)
        )
    }

    // 抽屉主体
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(320, easing = Motion.Bouncy)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(260, easing = Motion.Accelerate)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(300.dp)
                .shadow(12.dp, RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                .clip(RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4A3322),
                            Color(0xFF5C3A28),
                            Color(0xFF4A3322)
                        )
                    )
                )
                .statusBarsPadding()
        ) {
            // 标题栏
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Text(
                    text = "目录",
                    color = Color(0xFFF5EFE0),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "共 ${chapters.size} 章",
                    color = Color(0xFFF5EFE0).copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }

            // 章节列表
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(chapters) { index, chapter ->
                    val isCurrent = index == currentChapterIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    onChapterClick(index)
                                    onDismiss()
                                }
                            )
                            .background(
                                if (isCurrent) Color(0xFF6B4A35)
                                else Color.Transparent
                            )
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 当前章节指示条
                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFFD9A84E))
                            )
                            Spacer(Modifier.width(10.dp))
                        } else {
                            Spacer(Modifier.width(13.dp))
                        }
                        Text(
                            text = chapter.title,
                            color = if (isCurrent) Color(0xFFD9A84E) else Color(0xFFF5EFE0).copy(alpha = 0.8f),
                            fontSize = 15.sp,
                            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // 底部留白
                item {
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
