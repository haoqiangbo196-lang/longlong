package com.elegant.reader.ui.reader

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Tune
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
import com.elegant.reader.reader.Page
import com.elegant.reader.reader.PageTurnMode
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
    // pages 必须响应式收集：分页是异步的，完成后才能触发页面重组显示文字
    val pages by viewModel.pages.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showToc by remember { mutableStateOf(false) }

    // 翻页方向（供动画判断），true=向后翻，false=向前翻
    var pageTurnForward by remember { mutableStateOf(true) }

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

    // 音量键翻页（通过 MainActivity.dispatchKeyEvent 拦截 + 单例桥接）
    DisposableEffect(Unit) {
        com.elegant.reader.reader.VolumeKeyDispatcher.onVolumeDown = {
            pageTurnForward = true
            viewModel.nextPage()
        }
        com.elegant.reader.reader.VolumeKeyDispatcher.onVolumeUp = {
            pageTurnForward = false
            viewModel.prevPage()
        }
        onDispose {
            com.elegant.reader.reader.VolumeKeyDispatcher.clear()
        }
    }

    val density = LocalDensity.current.density
    val theme = settings.theme
    val backgroundColor = Color(theme.background)
    val textColor = Color(theme.textColor)

    // 统一的翻页动作，同时记录方向
    val doNextPage: () -> Unit = {
        pageTurnForward = true
        viewModel.nextPage()
    }
    val doPrevPage: () -> Unit = {
        pageTurnForward = false
        viewModel.prevPage()
    }

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
                .pointerInput(currentChapterIndex, currentPageIndex, showMenu, settings.pageTurnMode) {
                    // 点击翻页（左 1/3 上一页，右 1/3 下一页，中间出菜单）
                    detectTapGestures(
                        onTap = { offset ->
                            if (showMenu) {
                                showMenu = false
                                return@detectTapGestures
                            }
                            val width = size.width.toFloat()
                            when {
                                offset.x < width / 3 -> doPrevPage()
                                offset.x > width * 2 / 3 -> doNextPage()
                                else -> showMenu = true
                            }
                        }
                    )
                }
                .pointerInput(currentChapterIndex, currentPageIndex, showMenu, settings.pageTurnMode) {
                    // 滑动翻页：左右滑动（或 SCROLL 模式上下滑动）
                    if (settings.pageTurnMode == PageTurnMode.SCROLL) {
                        var totalDrag = 0f
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (totalDrag > 80f) doPrevPage()
                                else if (totalDrag < -80f) doNextPage()
                                totalDrag = 0f
                            },
                            onDragCancel = { totalDrag = 0f },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                            }
                        )
                    } else {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (totalDrag > 80f) doPrevPage()
                                else if (totalDrag < -80f) doNextPage()
                                totalDrag = 0f
                            },
                            onDragCancel = { totalDrag = 0f },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                            }
                        )
                    }
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
                        val forward = pageTurnForward
                        when (settings.pageTurnMode) {
                            PageTurnMode.SCROLL -> {
                                val slide = if (forward) 1 else -1
                                (slideInVertically(
                                    initialOffsetY = { fullHeight -> fullHeight * slide },
                                    animationSpec = tween(300, easing = Motion.Standard)
                                ) + fadeIn(tween(200, easing = Motion.Standard)))
                                    .togetherWith(
                                        slideOutVertically(
                                            targetOffsetY = { fullHeight -> -fullHeight * slide / 3 },
                                            animationSpec = tween(300, easing = Motion.Standard)
                                        ) + fadeOut(tween(180, easing = Motion.Accelerate))
                                    )
                            }

                            else -> {
                                // 左右滑动翻页
                                if (forward) {
                                    (slideInHorizontally(
                                        initialOffsetX = { fullWidth -> fullWidth },
                                        animationSpec = tween(320, easing = Motion.Standard)
                                    ) + fadeIn(tween(200, easing = Motion.Standard)))
                                        .togetherWith(
                                            slideOutHorizontally(
                                                targetOffsetX = { fullWidth -> -fullWidth },
                                                animationSpec = tween(320, easing = Motion.Standard)
                                            ) + fadeOut(tween(200, easing = Motion.Accelerate))
                                        )
                                } else {
                                    (slideInHorizontally(
                                        initialOffsetX = { fullWidth -> -fullWidth },
                                        animationSpec = tween(320, easing = Motion.Standard)
                                    ) + fadeIn(tween(200, easing = Motion.Standard)))
                                        .togetherWith(
                                            slideOutHorizontally(
                                                targetOffsetX = { fullWidth -> fullWidth },
                                                animationSpec = tween(320, easing = Motion.Standard)
                                            ) + fadeOut(tween(200, easing = Motion.Accelerate))
                                        )
                                }
                            }
                        }
                    },
                    label = "pageTurn",
                    modifier = Modifier.fillMaxSize(),
                    contentKey = { it.first to it.second }
                ) { (pageIndex, chapterIndex) ->
                    // 使用响应式收集的 pages：异步分页完成后自动刷新显示
                    val page = pages.getOrNull(pageIndex) ?: Page(0, emptyList())
                    if (page.lines.isEmpty() && pages.isEmpty()) {
                        // 分页尚未完成，显示加载中
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("加载中...", color = textColor.copy(alpha = 0.6f), fontSize = 15.sp)
                        }
                    } else {
                        TextPageCanvas(
                            page = page,
                            textColor = textColor,
                            textSizePx = settings.fontSizeSp * density,
                            modifier = Modifier.fillMaxSize()
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
                onToggleToc = { showToc = !showToc },
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
                    text = if (pages.isNotEmpty())
                        "${currentPageIndex + 1}/${pages.size}" else "",
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }

        // 设置面板（ModalBottomSheet，自带遮罩/下滑/返回键关闭）
        if (showSettings) {
            ReaderSettingsPanel(
                settings = settings,
                onSettingsChange = { newSettings ->
                    viewModel.updateSettings(newSettings)
                    scope.launch { settingsStore.save(newSettings) }
                },
                onDismiss = { showSettings = false }
            )
        }

        // 目录抽屉
        ChapterDrawer(
            visible = showToc,
            chapters = chapters,
            currentChapterIndex = currentChapterIndex,
            onChapterClick = { index -> viewModel.gotoChapter(index) },
            onDismiss = { showToc = false }
        )
    }

    // 返回键处理：设置面板/目录打开时优先关闭，其次关闭菜单
    androidx.activity.compose.BackHandler(enabled = showSettings) {
        showSettings = false
    }
    androidx.activity.compose.BackHandler(enabled = showToc && !showSettings) {
        showToc = false
    }
    androidx.activity.compose.BackHandler(enabled = showMenu && !showSettings && !showToc) {
        showMenu = false
    }
}

@Composable
private fun ReaderTopBar(
    title: String,
    onBack: () -> Unit,
    onToggleSettings: () -> Unit,
    onToggleToc: () -> Unit,
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
            contentDescription = "目录",
            tint = textColor,
            modifier = Modifier
                .clickableNoIndication(onClick = onToggleToc)
                .padding(8.dp)
        )
        Icon(
            imageVector = Icons.Rounded.Tune,
            contentDescription = "设置",
            tint = textColor,
            modifier = Modifier
                .clickableNoIndication(onClick = onToggleSettings)
                .padding(8.dp)
        )
    }
}
