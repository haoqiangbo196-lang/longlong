package com.elegant.reader.ui.reader

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elegant.reader.reader.ReaderSettingsStore
import com.elegant.reader.reader.ReaderViewModel
import com.elegant.reader.ui.animation.Motion
import com.elegant.reader.ui.clickableNoIndication
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * 阅读器主屏幕。
 */
@Composable
fun ReaderScreen(
    bookId: Long,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val book by viewModel.book.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val currentChapterIndex by viewModel.currentChapterIndex.collectAsState()
    val currentPageIndex by viewModel.currentPageIndex.collectAsState()
    val loading by viewModel.loading.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val context = LocalView.current.context
    val settingsStore = remember { ReaderSettingsStore(context) }
    val scope = rememberCoroutineScope()

    // 加载书籍
    LaunchedEffect(bookId) {
        viewModel.init(bookId)
    }

    // 从 DataStore 恢复上次的阅读设置（仅取一次初始值）
    LaunchedEffect(Unit) {
        val saved = settingsStore.settingsFlow.firstOrNull()
        if (saved != null) {
            viewModel.updateSettings(saved)
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.saveProgress() }
    }

    val density = LocalDensity.current.density
    val theme = settings.theme
    val backgroundColor = Color(theme.background)
    val textColor = Color(theme.textColor)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        val contentWidth = (maxWidth - settings.pageMarginHorizontalDp.dp * 2)
        val contentHeight = (maxHeight - (settings.pageMarginTopDp.dp + 40.dp) - (settings.pageMarginBottomDp.dp + 24.dp))

        // 更新分页尺寸
        LaunchedEffect(contentWidth, contentHeight) {
            val widthPx = (contentWidth.value * density).toInt()
            val heightPx = (contentHeight.value * density).toInt()
            viewModel.updatePageSize(widthPx, heightPx, density)
        }

        // 正文区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = settings.pageMarginHorizontalDp.dp,
                    end = settings.pageMarginHorizontalDp.dp,
                    top = settings.pageMarginTopDp.dp + 40.dp,
                    bottom = settings.pageMarginBottomDp.dp + 24.dp
                )
                .pointerInput(currentChapterIndex, currentPageIndex) {
                    detectTapGestures(
                        onTap = { offset ->
                            val width = size.width.toFloat()
                            when {
                                offset.x < width / 3 -> viewModel.prevPage()
                                offset.x > width * 2 / 3 -> viewModel.nextPage()
                                else -> showMenu = !showMenu
                            }
                        }
                    )
                }
                .pointerInput(settings.pageTurnMode) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // 滑动翻页：由点击区域之外的滑动触发
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                        }
                    )
                }
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("加载中...", color = textColor.copy(alpha = 0.6f), fontSize = 15.sp)
                }
            } else {
                AnimatedContent(
                    targetState = currentPageIndex to currentChapterIndex,
                    transitionSpec = {
                        val forward = targetState.first > initialState.first ||
                                targetState.second > initialState.second
                        if (forward) {
                            (slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(380, easing = Motion.PageTurn)
                            ) + fadeIn(tween(240, easing = Motion.Standard)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> -fullWidth / 3 },
                                        animationSpec = tween(380, easing = Motion.PageTurn)
                                    ) + fadeOut(tween(200, easing = Motion.Accelerate))
                                )
                        } else {
                            (slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(380, easing = Motion.PageTurn)
                            ) + fadeIn(tween(240, easing = Motion.Standard)))
                                .togetherWith(
                                    slideOutHorizontally(
                                        targetOffsetX = { fullWidth -> fullWidth / 3 },
                                        animationSpec = tween(380, easing = Motion.PageTurn)
                                    ) + fadeOut(tween(200, easing = Motion.Accelerate))
                                )
                        }
                    },
                    label = "pageTurn",
                    modifier = Modifier.fillMaxSize(),
                    contentKey = { it.first }
                ) { (pageIndex, chapterIndex) ->
                    val chapter = chapters.getOrNull(chapterIndex)
                    val pages = viewModel.pages.value
                    val pageText = pages.getOrNull(pageIndex)?.text ?: ""
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = pageText,
                            color = textColor,
                            fontSize = settings.fontSizeSp.sp,
                            lineHeight = (settings.fontSizeSp * settings.lineHeightMultiplier).sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }

        // 顶部栏
        if (showMenu) {
            ReaderTopBar(
                title = book?.title ?: "",
                onBack = onBack,
                onToggleSettings = { showSettings = !showSettings },
                backgroundColor = backgroundColor,
                textColor = textColor
            )
        }

        // 底部进度条
        if (showMenu) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chapters.getOrNull(currentChapterIndex)?.title ?: "",
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (viewModel.pages.value.isNotEmpty())
                        "${currentPageIndex + 1}/${viewModel.pages.value.size}" else "",
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }

        // 设置面板
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            ReaderSettingsPanel(
                visible = showSettings,
                settings = settings,
                onSettingsChange = { newSettings ->
                    viewModel.updateSettings(newSettings)
                    scope.launch { settingsStore.save(newSettings) }
                },
                onDismiss = { showSettings = false }
            )
        }
    }
}

@Composable
private fun ReaderTopBar(
    title: String,
    onBack: () -> Unit,
    onToggleSettings: () -> Unit,
    backgroundColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(backgroundColor.copy(alpha = 0.95f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.ArrowBack,
            contentDescription = "返回",
            tint = textColor,
            modifier = Modifier
                .clickableNoIndication(onClick = onBack)
                .padding(8.dp)
        )
        Text(
            text = title,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        )
        Icon(
            imageVector = Icons.Rounded.FormatListBulleted,
            contentDescription = "设置",
            tint = textColor,
            modifier = Modifier
                .clickableNoIndication(onClick = onToggleSettings)
                .padding(8.dp)
        )
    }
}
