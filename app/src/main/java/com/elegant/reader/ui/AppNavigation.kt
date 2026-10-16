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
        enterTransition = {
            fadeIn(tween(260, easing = Motion.Standard)) +
                    slideInHorizontally(
                        initialOffsetX = { it / 4 },
                        animationSpec = tween(320, easing = Motion.Standard)
                    )
        },
        exitTransition = {
            fadeOut(tween(200, easing = Motion.Accelerate)) +
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(260, easing = Motion.Standard)
                    )
        },
        popEnterTransition = {
            fadeIn(tween(260, easing = Motion.Standard))
        },
        popExitTransition = {
            fadeOut(tween(200, easing = Motion.Accelerate))
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
