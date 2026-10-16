package com.elegant.reader.ui.shelf

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.elegant.reader.ui.theme.GreenLight
import com.elegant.reader.ui.theme.GreenMain
import com.elegant.reader.ui.theme.InkDark
import com.elegant.reader.ui.theme.InkGray
import com.elegant.reader.ui.theme.WoodTone

/**
 * 书架条目 —— 完全按设计稿还原：
 * 书封（竖排书名 + 底部木托）+ 封面下方书名 + 进度条。
 * 单击打开，长按删除。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookCoverCard(
    book: Book,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val gradient = CoverGradients[(book.id % CoverGradients.size).toInt()]
    val titleChars = remember(book.title) {
        if (book.title.length > 6) (book.title.take(5) + "…").toList() else book.title.toList()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ---- 书封 ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .shadow(4.dp, RoundedCornerShape(6.dp), spotColor = Color.Black.copy(alpha = 0.25f))
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.linearGradient(
                        colors = gradient,
                        start = Offset.Zero,
                        end = Offset(600f, 900f)
                    )
                )
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        ) {
            // 竖排书名（白字带轻阴影）
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                titleChars.forEach { ch ->
                    Text(
                        text = ch.toString(),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.4f),
                                offset = Offset(1f, 1f),
                                blurRadius = 2f
                            )
                        )
                    )
                }
            }
            // 底部木托
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFD9BC8C), WoodTone, Color(0xFFA8865C))
                        )
                    )
            )
        }

        Spacer(Modifier.height(7.dp))

        // ---- 封面下方：书名 ----
        Text(
            text = book.title,
            color = InkDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(5.dp))

        // ---- 进度行：百分比 + 进度条 ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${(book.progress * 100).toInt()}%",
                color = InkGray,
                fontSize = 10.sp
            )
            Spacer(Modifier.width(5.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GreenLight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(book.progress.coerceIn(0.02f, 1f))
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(GreenMain)
                )
            }
        }
    }
}
