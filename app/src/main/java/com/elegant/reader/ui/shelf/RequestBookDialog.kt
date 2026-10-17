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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elegant.reader.data.BookRequest
import com.elegant.reader.ui.theme.CardCream
import com.elegant.reader.ui.theme.GreenLight
import com.elegant.reader.ui.theme.GreenMain
import com.elegant.reader.ui.theme.InkDark
import com.elegant.reader.ui.theme.InkGray
import com.elegant.reader.ui.theme.PageBg

/**
 * 求书弹窗：登记想看的书 + 查看我的求书记录 + 下载已到书。
 */
@Composable
fun RequestBookDialog(
    viewModel: ShelfViewModel,
    onDismiss: () -> Unit
) {
    val requests by viewModel.requests.collectAsStateWithLifecycle()
    val submitting by viewModel.requestSubmitting.collectAsStateWithLifecycle()
    val message by viewModel.requestMessage.collectAsStateWithLifecycle()
    val downloadingId by viewModel.downloadingId.collectAsStateWithLifecycle()

    var bookTitle by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    // 打开弹窗即刷新我的求书记录
    LaunchedEffect(Unit) { viewModel.refreshRequests() }
    // 有提示时自动清除（5 秒）
    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(5000)
            viewModel.clearRequestMessage()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(PageBg)
                .padding(20.dp)
        ) {
            // 标题
            Text(
                text = "求书",
                color = InkDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "登记想看的书，找到后自动送进书架",
                color = InkGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(Modifier.height(16.dp))

            // 书名输入
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardCream)
                    .border(1.dp, Color(0xFFE0D8C8), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                BasicTextField(
                    value = bookTitle,
                    onValueChange = { bookTitle = it },
                    textStyle = TextStyle(color = InkDark, fontSize = 15.sp),
                    cursorBrush = SolidColor(GreenMain),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (bookTitle.isEmpty()) {
                            Text("想看的书名", color = InkGray, fontSize = 15.sp)
                        }
                        inner()
                    }
                )
            }
            Spacer(Modifier.height(10.dp))

            // 备注输入
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardCream)
                    .border(1.dp, Color(0xFFE0D8C8), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                BasicTextField(
                    value = note,
                    onValueChange = { note = it },
                    textStyle = TextStyle(color = InkDark, fontSize = 14.sp),
                    cursorBrush = SolidColor(GreenMain),
                    minLines = 2,
                    maxLines = 3,
                    decorationBox = { inner ->
                        if (note.isEmpty()) {
                            Text("备注（可选，如指定版本）", color = InkGray, fontSize = 14.sp)
                        }
                        inner()
                    }
                )
            }
            Spacer(Modifier.height(14.dp))

            // 提交按钮
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GreenMain)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !submitting
                    ) {
                        viewModel.submitRequest(bookTitle, note)
                        bookTitle = ""
                        note = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (submitting) "提交中…" else "登记求书",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 提示消息
            message?.let { msg ->
                Spacer(Modifier.height(10.dp))
                Text(
                    text = msg,
                    color = if (msg.contains("成功") || msg.contains("加入")) GreenMain else Color(0xFFB3402E),
                    fontSize = 12.sp
                )
            }

            // 我的求书记录
            if (requests.isNotEmpty()) {
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "我的求书",
                    color = InkDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(requests) { _, item ->
                        RequestItem(
                            item = item,
                            downloading = item.objectId == downloadingId,
                            onDownload = { viewModel.downloadRequestBook(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestItem(
    item: BookRequest,
    downloading: Boolean,
    onDownload: () -> Unit
) {
    val isReady = item.status == "ready" && item.bookFileUrl.isNotBlank()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardCream)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "《${item.bookTitle}》",
                color = InkDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 状态标签
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isReady) GreenLight else Color(0xFFEFE9DC))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isReady) "书已到" else "寻找中",
                        color = if (isReady) Color(0xFF3D5A48) else InkGray,
                        fontSize = 10.sp
                    )
                }
                if (isReady) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "点右侧下载 →",
                        color = InkGray,
                        fontSize = 10.sp
                    )
                }
            }
        }
        if (isReady) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(GreenMain)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !downloading
                    ) { onDownload() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (downloading) "…" else "收",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
