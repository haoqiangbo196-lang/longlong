package com.elegant.reader.ui.shelf

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elegant.reader.data.Book
import com.elegant.reader.ui.animation.Motion
import com.elegant.reader.ui.clickableNoIndication
import com.elegant.reader.ui.theme.TextPrimary
import com.elegant.reader.ui.theme.TextSecondary
import com.elegant.reader.ui.theme.TextTertiary
import com.elegant.reader.ui.theme.Terracotta
import com.elegant.reader.ui.theme.TerracottaLight
import com.elegant.reader.ui.theme.WarmBackground
import com.elegant.reader.ui.theme.WarmSurface

/**
 * 书架主界面。
 */
@Composable
fun ShelfScreen(
    onOpenBook: (Long) -> Unit,
    viewModel: ShelfViewModel = viewModel()
) {
    val books by viewModel.books
    val importing by viewModel.importing

    var showDeleteDialog by remember { mutableStateOf<Book?>(null) }

    // 文件选择器
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importBook(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        WarmBackground,
                        WarmBackground,
                        WarmSurface.copy(alpha = 0.6f)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部标题栏
            ShelfHeader(bookCount = books.size)

            if (books.isEmpty()) {
                // 空状态
                EmptyShelf(
                    onImport = {
                        filePicker.launch(arrayOf("text/plain", "application/epub+zip", "application/octet-stream"))
                    }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 12.dp,
                        bottom = 100.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = books,
                        key = { it.id }
                    ) { book ->
                        var visible by remember { mutableStateOf(false) }
                        // 使用启动时淡入 + 上移的入场动画
                        androidx.compose.runtime.LaunchedEffect(book.id) {
                            visible = true
                        }
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(animationSpec = tween(500, easing = Motion.Emphasized)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 4 },
                                        animationSpec = tween(500, easing = Motion.Standard)
                                    ) +
                                    scaleIn(
                                        initialScale = 0.9f,
                                        animationSpec = tween(500, easing = Motion.Emphasized)
                                    )
                        ) {
                            BookCoverCard(
                                book = book,
                                onClick = { onOpenBook(book.id) }
                            )
                        }
                    }
                }
            }
        }

        // 底部悬浮导入按钮
        FloatingImportButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            loading = importing,
            onClick = {
                filePicker.launch(arrayOf("text/plain", "application/epub+zip", "application/octet-stream"))
            }
        )
    }

    // 删除确认
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
                    Text("删除", color = Color(0xFFC05B4D))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun ShelfHeader(bookCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "我的书架",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (bookCount == 0) "添加你的第一本书吧" else "共 $bookCount 本书",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
            Icon(
                imageVector = Icons.Rounded.MenuBook,
                contentDescription = null,
                tint = Terracotta,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(TerracottaLight.copy(alpha = 0.25f))
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun EmptyShelf(onImport: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(TerracottaLight.copy(alpha = 0.5f), Terracotta.copy(alpha = 0.3f))
                    )
                )
                .padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MenuBook,
                contentDescription = null,
                tint = Terracotta,
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "书架空空如也",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "点击下方按钮，导入 TXT 或 EPUB 书籍",
            color = TextSecondary,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(28.dp))
        androidx.compose.material3.Button(
            onClick = onImport,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .height(52.dp)
                .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Terracotta.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null)
            Spacer(Modifier.height(0.dp))
            Text("导入书籍", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun FloatingImportButton(
    modifier: Modifier = Modifier,
    loading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                ambientColor = Terracotta.copy(alpha = 0.4f),
                spotColor = Terracotta.copy(alpha = 0.5f)
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(TerracottaLight, Terracotta)
                )
            )
            .clickableNoIndication(onClick = onClick)
            .padding(18.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "导入书籍",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}
