package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.viewmodel.MarklifyViewModel

@Composable
fun AppNavigation(
    viewModel: MarklifyViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.DASHBOARD
    ) {
        // 1. Dashboard Screen
        composable(NavRoutes.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToTopics = {
                    navController.navigate(NavRoutes.TOPICS)
                },
                onNavigateToTopicDetail = { topicId ->
                    navController.navigate(NavRoutes.topicDetail(topicId))
                },
                onNavigateToTestDetail = { testId ->
                    navController.navigate(NavRoutes.testDetail(testId))
                },
                onNavigateToSheetGenerator = { testId ->
                    navController.navigate(NavRoutes.sheetGenerator(testId))
                },
                onNavigateToScanner = { testId ->
                    navController.navigate(NavRoutes.scanner(testId))
                },
                onNavigateToResultDetail = { resultId ->
                    navController.navigate(NavRoutes.resultDetail(resultId))
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                },
                onNavigateToHistory = {
                    navController.navigate(NavRoutes.HISTORY)
                }
            )
        }

        // 2. Topics Screen
        composable(NavRoutes.TOPICS) {
            TopicsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTopicDetail = { topicId ->
                    navController.navigate(NavRoutes.topicDetail(topicId))
                }
            )
        }

        // 3. Topic Detail Screen
        composable(
            route = NavRoutes.TOPIC_DETAIL,
            arguments = listOf(navArgument("topicId") { type = NavType.LongType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getLong("topicId") ?: 0L
            TopicDetailScreen(
                topicId = topicId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTestDetail = { testId ->
                    navController.navigate(NavRoutes.testDetail(testId))
                },
                onNavigateToSheetGenerator = { testId ->
                    navController.navigate(NavRoutes.sheetGenerator(testId))
                },
                onNavigateToScanner = { testId ->
                    navController.navigate(NavRoutes.scanner(testId))
                }
            )
        }

        // 4. Test Detail Screen
        composable(
            route = NavRoutes.TEST_DETAIL,
            arguments = listOf(navArgument("testId") { type = NavType.LongType })
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getLong("testId") ?: 0L
            TestDetailScreen(
                testId = testId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTestEditor = { id ->
                    navController.navigate(NavRoutes.testEditor(id))
                },
                onNavigateToSheetGenerator = { id ->
                    navController.navigate(NavRoutes.sheetGenerator(id))
                },
                onNavigateToScanner = { id ->
                    navController.navigate(NavRoutes.scanner(id))
                },
                onNavigateToResults = { id ->
                    navController.navigate(NavRoutes.results(id))
                }
            )
        }

        // 5. Test Editor Screen
        composable(
            route = NavRoutes.TEST_EDITOR,
            arguments = listOf(navArgument("testId") { type = NavType.LongType })
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getLong("testId") ?: 0L
            TestEditorScreen(
                testId = testId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. Blank Sheet Generator Screen
        composable(
            route = NavRoutes.SHEET_GENERATOR,
            arguments = listOf(navArgument("testId") { type = NavType.LongType })
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getLong("testId") ?: 0L
            SheetGeneratorScreen(
                testId = testId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScanner = { id ->
                    navController.navigate(NavRoutes.scanner(id))
                }
            )
        }

        // 7. Scanner Screen
        composable(
            route = NavRoutes.SCANNER,
            arguments = listOf(navArgument("testId") { type = NavType.LongType })
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getLong("testId") ?: 0L
            ScannerScreen(
                testId = testId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReview = {
                    navController.navigate(NavRoutes.REVIEW)
                }
            )
        }

        // 8. Review Screen
        composable(NavRoutes.REVIEW) {
            ReviewScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onConfirmAndScore = { resultId ->
                    navController.navigate(NavRoutes.resultDetail(resultId)) {
                        popUpTo(NavRoutes.DASHBOARD) { inclusive = false }
                    }
                }
            )
        }

        // 9. Results Screen
        composable(
            route = NavRoutes.RESULTS,
            arguments = listOf(navArgument("testId") { type = NavType.LongType })
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getLong("testId") ?: 0L
            ResultsScreen(
                testId = testId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResultDetail = { resultId ->
                    navController.navigate(NavRoutes.resultDetail(resultId))
                },
                onNavigateToScanner = { id ->
                    navController.navigate(NavRoutes.scanner(id))
                }
            )
        }

        // 10. Result Detail Screen
        composable(
            route = NavRoutes.RESULT_DETAIL,
            arguments = listOf(navArgument("resultId") { type = NavType.LongType })
        ) { backStackEntry ->
            val resultId = backStackEntry.arguments?.getLong("resultId") ?: 0L
            ResultDetailScreen(
                resultId = resultId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 11. Scanner Settings & Threshold Calibration Screen
        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 12. Graded Tests History Screen
        composable(NavRoutes.HISTORY) {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResultDetail = { resultId ->
                    navController.navigate(NavRoutes.resultDetail(resultId))
                }
            )
        }
    }
}
