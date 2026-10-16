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
 * 书籍封面卡片，模拟精装书封的拟物质感。
 * 用渐变 + 书脊高光 + 上下压暗营造立体感。
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
            .width(120.dp)
            .clip(RoundedCornerShape(14.dp))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .background(
                Brush.linearGradient(
                    colors = gradient,
                    start = Offset(0f, 0f),
                    end = Offset(1200f, 1600f)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            // 书脊高光
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(4.dp)
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.18f))
            )
            // 上下压暗，营造内阴影
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.08f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.12f)
                            )
                        )
                    )
            )
            // 标题文字
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = book.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
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
        // 底部信息条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.18f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
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
