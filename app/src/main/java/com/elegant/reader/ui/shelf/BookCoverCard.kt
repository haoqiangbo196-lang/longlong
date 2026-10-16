package com.elegant.reader.ui.shelf

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
 * 书籍封面 —— 古典书卷风。
 *
 * 竖排书名 + 书脊高光 + 上下压暗 + 底部进度条，像一本立在书架上的真书。
 */
@Composable
fun BookCoverCard(
    book: Book,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val gradient = CoverGradients[(book.id % CoverGradients.size).toInt()]
    // 竖排书名（最多 7 字，超出省略）
    val titleChars = remember(book.title) {
        if (book.title.length > 7) (book.title.take(6) + "…").toList() else book.title.toList()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(196.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 10.dp, bottomEnd = 4.dp, bottomStart = 10.dp),
                ambientColor = Color.Black.copy(alpha = 0.45f),
                spotColor = Color.Black.copy(alpha = 0.55f)
            )
            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 10.dp, bottomEnd = 4.dp, bottomStart = 10.dp))
            .background(
                Brush.linearGradient(
                    colors = gradient,
                    start = Offset(0f, 0f),
                    end = Offset(900f, 1600f)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // 书脊高光（左缘亮条，像书脊被灯光照亮）
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(6.dp)
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
                )
        )
        // 右缘暗边（背光面）
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(4.dp)
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.22f))
        )
        // 上下压暗（内阴影立体感）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.18f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.22f)
                        )
                    )
                )
        )
        // 竖排书名
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            titleChars.forEach { ch ->
                Text(
                    text = ch.toString(),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 23.sp
                )
            }
        }
        // 底部进度条
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.30f))
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(book.progress * 100).toInt()}%",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
