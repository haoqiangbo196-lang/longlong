package com.elegant.reader.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.elegant.reader.ui.animation.Motion
import com.elegant.reader.ui.reader.ReaderScreen
import com.elegant.reader.ui.shelf.ShelfScreen

/**
 * 应用导航。
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "shelf",
        // 进入阅读器：从右侧滑入
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(360, easing = Motion.Standard)
            ) + fadeIn(tween(260, easing = Motion.Standard))
        },
        // 离开书架：向左滑出
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(360, easing = Motion.Standard)
            ) + fadeOut(tween(260, easing = Motion.Accelerate))
        },
        // 返回书架：从左侧滑入（明显动画，不再生硬）
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(340, easing = Motion.Standard)
            ) + fadeIn(tween(260, easing = Motion.Standard))
        },
        // 退出阅读器：向右滑出
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(340, easing = Motion.Standard)
            ) + fadeOut(tween(260, easing = Motion.Accelerate))
        }
    ) {
        composable("shelf") {
            ShelfScreen(
                onOpenBook = { bookId ->
                    navController.navigate("reader/$bookId")
                }
            )
        }

        composable(
            route = "reader/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
            ReaderScreen(
                bookId = bookId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
