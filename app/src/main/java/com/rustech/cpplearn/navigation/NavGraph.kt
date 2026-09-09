package com.rustech.cpplearn.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.rustech.cpplearn.ui.screen.HomeScreen
import com.rustech.cpplearn.ui.screen.LeaderboardScreen
import com.rustech.cpplearn.ui.screen.LessonScreen
import com.rustech.cpplearn.ui.screen.LoginScreen
import com.rustech.cpplearn.ui.screen.ProfileScreen
import com.rustech.cpplearn.ui.screen.QuizScreen
import com.rustech.cpplearn.ui.screen.RegisterScreen
import com.rustech.cpplearn.ui.screen.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val LESSON = "lesson/{lessonId}"
    const val QUIZ = "quiz/{lessonId}"
    const val LEADERBOARD = "leaderboard"
    const val PROFILE = "profile"

    fun lesson(lessonId: String) = "lesson/$lessonId"
    fun quiz(lessonId: String) = "quiz/$lessonId"
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(onFinished = { loggedIn ->
                val destination = if (loggedIn) Routes.HOME else Routes.LOGIN
                navController.navigate(destination) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onLessonClick = { lesson -> navController.navigate(Routes.lesson(lesson.id)) },
                onLeaderboardClick = { navController.navigate(Routes.LEADERBOARD) },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(
            route = Routes.LESSON,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: return@composable
            LessonScreen(
                lessonId = lessonId,
                onQuizClick = { id -> navController.navigate(Routes.quiz(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.QUIZ,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: return@composable
            QuizScreen(
                lessonId = lessonId,
                onFinished = { _, _ ->
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LEADERBOARD) {
            LeaderboardScreen()
        }

        composable(Routes.PROFILE) {
            ProfileScreen(onLoggedOut = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
    }
}
