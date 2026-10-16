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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elegant.reader.data.Book
import com.elegant.reader.ui.animation.Motion
import com.elegant.reader.ui.clickableNoIndication
import com.elegant.reader.ui.theme.LeatherBrown
import com.elegant.reader.ui.theme.LeatherDark
import com.elegant.reader.ui.theme.PaperCream
import com.elegant.reader.ui.theme.PaperWhite
import com.elegant.reader.ui.theme.Terracotta
import com.elegant.reader.ui.theme.TerracottaLight
import com.elegant.reader.ui.theme.TextSecondary
import com.elegant.reader.ui.theme.WoodDark
import com.elegant.reader.ui.theme.WoodLight
import com.elegant.reader.ui.theme.WoodMid

/**
 * 书架主界面 —— 锤子 Smartisan OS 拟物风格。
 *
 * 设计语言：
 *  - 木纹渐变背景，营造真实书架质感
 *  - 皮革质感顶栏，书脊拟物卡片
 *  - 多层次阴影 + 浮雕高光，立体浮起
 */
@Composable
fun ShelfScreen(
    onOpenBook: (Long) -> Unit,
    viewModel: ShelfViewModel = viewModel()
) {
    val books by viewModel.books.collectAsState()
    val importing by viewModel.importing.collectAsState()

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
                        WoodLight,
                        WoodMid,
                        WoodDark
                    ),
                    startY = 0f,
                    endY = 1400f
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 皮革质感顶栏
            ShelfHeader(bookCount = books.size)

            if (books.isEmpty()) {
                EmptyShelf(
                    onImport = {
                        filePicker.launch(arrayOf("text/plain", "application/epub+zip", "application/octet-stream"))
                    }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 124.dp),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 16.dp,
                        bottom = 110.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = books,
                        key = { it.id }
                    ) { book ->
                        var visible by remember { mutableStateOf(false) }
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

        // 底部悬浮导入按钮（拟物金属质感）
        FloatingImportButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(26.dp),
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

/**
 * 皮革质感顶栏。
 */
@Composable
private fun ShelfHeader(bookCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LeatherDark,
                        LeatherBrown,
                        LeatherBrown
                    )
                )
            )
            // 皮革底部缝线阴影
            .shadow(6.dp, spotColor = Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "龙颜大阅",
            color = PaperCream,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (bookCount == 0) "藏书阁空置，静候佳卷" else "藏书 $bookCount 卷",
            color = PaperCream.copy(alpha = 0.7f),
            fontSize = 14.sp,
            letterSpacing = 1.sp
        )
    }
}

/**
 * 空书架状态。
 */
@Composable
private fun EmptyShelf(onImport: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 拟物书卷图标
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(28.dp))
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor = Color.Black.copy(alpha = 0.4f)
                )
                .background(
                    Brush.linearGradient(
                        colors = listOf(PaperWhite, PaperCream),
                        start = Offset(0f, 0f),
                        end = Offset(0f, 400f)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "龍",
                    color = Terracotta,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "阅",
                    color = TextSecondary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(Modifier.height(28.dp))
        Text(
            text = "书架空空如也",
            color = PaperWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "点击下方按钮，导入 TXT 或 EPUB 书籍",
            color = PaperWhite.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(28.dp))
        // 拟物按钮
        Box(
            modifier = Modifier
                .height(52.dp)
                .clip(RoundedCornerShape(18.dp))
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = Terracotta.copy(alpha = 0.5f),
                    spotColor = Terracotta.copy(alpha = 0.6f)
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(TerracottaLight, Terracotta)
                    )
                )
                .clickableNoIndication(onClick = onImport)
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White)
                Spacer(Modifier.size(6.dp))
                Text("导入书籍", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * 拟物金属质感悬浮导入按钮。
 */
@Composable
private fun FloatingImportButton(
    modifier: Modifier = Modifier,
    loading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 14.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        TerracottaLight,
                        Terracotta,
                        WoodDark
                    ),
                    center = Offset(200f, 200f),
                    radius = 600f
                )
            )
            .clickableNoIndication(onClick = onClick)
            .padding(20.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "导入书籍",
            tint = Color.White,
            modifier = Modifier.size(30.dp)
        )
    }
}
