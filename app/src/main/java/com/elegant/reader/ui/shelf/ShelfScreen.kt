package com.elegant.reader.ui.shelf

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elegant.reader.R
import com.elegant.reader.data.Book
import com.elegant.reader.ui.theme.CardCream
import com.elegant.reader.ui.theme.GreenMain
import com.elegant.reader.ui.theme.InkDark
import com.elegant.reader.ui.theme.InkGray
import com.elegant.reader.ui.theme.PageBg

/**
 * 书架主界面 —— 完全按设计稿还原：
 * 米色背景 + 头图（龙颜大阅·萌龙）+ 添加书籍卡片 + 我的书架四列书架。
 */
@Composable
fun ShelfScreen(
    onOpenBook: (Long) -> Unit,
    viewModel: ShelfViewModel = viewModel()
) {
    val books by viewModel.books.collectAsState()
    val importing by viewModel.importing.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<Book?>(null) }
    var showRequestDialog by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importBook(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PageBg)
            .statusBarsPadding()
    ) {
        // ---- 头图（龙颜大阅·萌龙·山水） ----
        Image(
            painter = painterResource(R.drawable.home_banner),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )

        // ---- 添加书籍卡片 ----
        AddBookCard(
            loading = importing,
            onClick = {
                filePicker.launch(arrayOf("text/plain", "application/epub+zip", "application/octet-stream"))
            },
            modifier = Modifier.padding(horizontal = 18.dp)
        )

        // ---- 求书入口 ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showRequestDialog = true },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "想看的书还没有？",
                color = InkGray,
                fontSize = 12.sp
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "点这里求书",
                color = GreenMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(22.dp))

        // ---- 我的书架标题 ----
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.shelf_title_icon),
                contentDescription = null,
                modifier = Modifier.height(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "我的书架",
                color = InkDark,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${books.size} 本",
                color = InkGray,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(14.dp))

        // ---- 书架网格（4 列，同设计稿） ----
        if (books.isEmpty()) {
            EmptyHint()
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = books,
                    key = { it.id }
                ) { book ->
                    BookCoverCard(
                        book = book,
                        onClick = { onOpenBook(book.id) },
                        onLongClick = { showDeleteDialog = book }
                    )
                }
            }
        }
    }

    // 长按删除确认
    showDeleteDialog?.let { book ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除书籍") },
            text = { Text("确定要删除《${book.title}》吗？此操作不可恢复。") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.deleteBook(book)
                        showDeleteDialog = null
                    }
                ) {
                    Text("删除", color = Color(0xFFB3402E))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 求书弹窗
    if (showRequestDialog) {
        RequestBookDialog(
            viewModel = viewModel,
            onDismiss = { showRequestDialog = false }
        )
    }
}

/**
 * 添加书籍卡片：书堆插画 + 文案 + 绿色圆形加号按钮。
 */
@Composable
private fun AddBookCard(
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(alpha = 0.06f))
            .clip(RoundedCornerShape(22.dp))
            .background(CardCream)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 书堆插画
        Image(
            painter = painterResource(R.drawable.add_books_icon),
            contentDescription = null,
            modifier = Modifier.height(46.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (loading) "正在导入…" else "添加书籍",
                color = InkDark,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "开启你的阅读之旅",
                color = InkGray,
                fontSize = 12.sp
            )
        }
        // 绿色圆形加号
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(GreenMain),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 空书架提示。
 */
@Composable
private fun EmptyHint() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "书架空空如也",
            color = InkDark,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "点击上方「添加书籍」，导入 TXT 或 EPUB",
            color = InkGray,
            fontSize = 13.sp
        )
    }
}
