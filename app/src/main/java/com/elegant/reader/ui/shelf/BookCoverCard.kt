package com.elegant.reader.ui.shelf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elegant.reader.data.Book
import com.elegant.reader.ui.theme.CoverGradients

/**
 * 书籍封面卡片 —— 锤子拟物精装书封。
 *
 * 渐变书封 + 书脊高光 + 上下压暗 + 浮雕标题，营造真实精装书质感。
 */
@Composable
fun BookCoverCard(
    book: Book,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val gradient = CoverGradients[(book.id % CoverGradients.size).toInt()]

    Column(
        modifier = modifier
            .width(124.dp)
            .clip(RoundedCornerShape(16.dp))
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.45f)
            )
            .background(
                Brush.linearGradient(
                    colors = gradient,
                    start = Offset(0f, 0f),
                    end = Offset(1240f, 1700f)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(168.dp)) {
            // 书脊高光（左侧亮条）
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(5.dp)
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    )
            )
            // 书脊阴影（右侧）
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(3.dp)
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f))
            )
            // 上下压暗，营造内阴影立体感
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.12f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.18f)
                            )
                        )
                    )
            )
            // 浮雕标题
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 浮雕阴影层（向下偏移的暗色文字）
                Text(
                    text = book.title,
                    color = Color.Black.copy(alpha = 0.35f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                // 主标题层
                Text(
                    text = book.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 21.sp,
                    modifier = Modifier.padding(top = 0.dp)
                )
                if (book.author.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = book.author,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        // 底部信息条（皮革质感）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.28f))
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${(book.progress * 100).toInt()}%",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = formatWords(book.totalWords),
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 9.sp
            )
        }
    }
}

private fun formatWords(words: Long): String {
    return when {
        words >= 10000 -> "${words / 10000}万字"
        words >= 1000 -> "${words / 1000}千字"
        else -> "${words}字"
    }
}
