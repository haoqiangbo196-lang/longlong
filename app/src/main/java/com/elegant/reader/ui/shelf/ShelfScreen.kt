package com.elegant.reader.ui.shelf

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elegant.reader.R
import com.elegant.reader.data.Book
import com.elegant.reader.ui.clickableNoIndication

// 书架配色（古典书房）
private val Ink = Color(0xFF2A1E14)
private val Gold = Color(0xFFE2B96F)
private val GoldDim = Color(0xFFC9A87A)
private val PaperWarm = Color(0xFFF3E9D5)
private val SealRed = Color(0xFFB3402E)

private val COLUMN_COUNT = 3

/**
 * 书架主界面 —— 古典书房·木质书架。
 *
 * 深色木纹背景，书本立在一排排木搁板上，竖排书名，
 * 金色标题 + 红色印章，古典雅致。
 */
@Composable
fun ShelfScreen(
    onOpenBook: (Long) -> Unit,
    viewModel: ShelfViewModel = viewModel()
) {
    val books by viewModel.books.collectAsState()
    val importing by viewModel.importing.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<Book?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importBook(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
    ) {
        // 木纹背景铺满
        Image(
            painter = painterResource(R.drawable.shelf_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // 顶部轻微压暗，让标题更清晰
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent)
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            ShelfHeader(bookCount = books.size)

            if (books.isEmpty()) {
                EmptyShelf(
                    onImport = {
                        filePicker.launch(arrayOf("text/plain", "application/epub+zip", "application/octet-stream"))
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 22.dp, end = 22.dp, top = 8.dp, bottom = 110.dp
                    )
                ) {
                    val rows = books.chunked(COLUMN_COUNT)
                    items(rows.size) { rowIndex ->
                        val rowBooks = rows[rowIndex]
                        BookRow(rowBooks = rowBooks, onOpenBook = onOpenBook)
                        ShelfBoard(isLast = rowIndex == rows.lastIndex)
                    }
                }
            }
        }

        // 底部悬浮导入按钮
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
                    Text("删除", color = SealRed)
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
 * 顶栏：金色标题 + 红印章 + 藏书数。
 */
@Composable
private fun ShelfHeader(bookCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "龙颜大阅",
                color = Gold,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (bookCount == 0) "藏书阁虚位以待" else "藏书 · $bookCount 卷",
                color = PaperWarm.copy(alpha = 0.65f),
                fontSize = 13.sp,
                letterSpacing = 2.sp
            )
        }
        // 红色印章
        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(4.dp, RoundedCornerShape(8.dp), spotColor = Color.Black.copy(alpha = 0.4f))
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFC2503C), SealRed))
                )
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "阅",
                color = PaperWarm,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 一行书 + 搁板（由调用方在行后画搁板）。
 */
@Composable
private fun BookRow(
    rowBooks: List<Book>,
    onOpenBook: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        repeat(COLUMN_COUNT) { index ->
            Box(modifier = Modifier.weight(1f)) {
                rowBooks.getOrNull(index)?.let { book ->
                    BookCoverCard(
                        book = book,
                        onClick = { onOpenBook(book.id) }
                    )
                }
            }
        }
    }
}

/**
 * 木质搁板：板面渐变 + 顶部高光 + 下方投影。
 */
@Composable
private fun ShelfBoard(isLast: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
    ) {
        // 板面（上亮下暗的木条）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(9.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFA87B54), Color(0xFF7A5236), Color(0xFF5C3A28))
                    )
                )
        )
        // 板前沿高光
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GoldDim.copy(alpha = 0.35f))
        )
        // 板下投影
        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.40f),
                                Color.Transparent
                            )
                        )
                    )
            )
        } else {
            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * 空书架。
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
        // 印章式空状态图标
        Box(
            modifier = Modifier
                .size(110.dp)
                .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFC2503C), SealRed))
                )
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "空",
                    color = PaperWarm,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(30.dp))
        Text(
            text = "书架空空如也",
            color = PaperWarm,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "点右下角按钮，导入 TXT 或 EPUB 书籍",
            color = PaperWarm.copy(alpha = 0.6f),
            fontSize = 13.sp
        )
        Spacer(Modifier.height(30.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.4f))
                .background(Gold)
                .clickableNoIndication(onClick = onImport)
                .padding(horizontal = 28.dp, vertical = 13.dp)
        ) {
            Text("导入书籍", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * 悬浮导入按钮：暗铜金属质感。
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
                ambientColor = Color.Black.copy(alpha = 0.45f),
                spotColor = Color.Black.copy(alpha = 0.55f)
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Gold, Color(0xFFB08A45), Color(0xFF8A6830)),
                    center = androidx.compose.ui.geometry.Offset(180f, 180f),
                    radius = 520f
                )
            )
            .clickableNoIndication(onClick = onClick)
            .padding(20.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "导入书籍",
            tint = Ink,
            modifier = Modifier.size(28.dp)
        )
    }
}
