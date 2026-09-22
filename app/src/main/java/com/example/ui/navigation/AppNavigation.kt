package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.viewmodel.MarklifyViewModel

data class BottomNavItem(val route: String, val title: String, val icon: ImageVector)

@Composable
fun AppNavigation(
    viewModel: MarklifyViewModel = viewModel()
) {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem(NavRoutes.DASHBOARD, "Exams", Icons.Default.Assignment),
        BottomNavItem(NavRoutes.ATTENDANCE, "Attendance", Icons.Default.PlaylistAddCheck),
        BottomNavItem(NavRoutes.CLASSES, "Classes", Icons.Default.Group),
        BottomNavItem(NavRoutes.MORE, "More", Icons.Default.MoreHoriz)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (bottomNavItems.any { it.route == currentRoute }) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.DASHBOARD,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Main Bottom Tabs
            composable(NavRoutes.DASHBOARD) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTopics = { navController.navigate(NavRoutes.TOPICS) },
                    onNavigateToTopicDetail = { topicId -> navController.navigate(NavRoutes.topicDetail(topicId)) },
                    onNavigateToTestDetail = { testId -> navController.navigate(NavRoutes.testDetail(testId)) },
                    onNavigateToSheetGenerator = { testId -> navController.navigate(NavRoutes.sheetGenerator(testId)) },
                    onNavigateToScanner = { testId -> navController.navigate(NavRoutes.scanner(testId)) },
                    onNavigateToResultDetail = { resultId -> navController.navigate(NavRoutes.resultDetail(resultId)) },
                    onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) },
                    onNavigateToHistory = { navController.navigate(NavRoutes.HISTORY) },
                    onNavigateToSaveExam = { navController.navigate(NavRoutes.SAVE_EXAM) }
                )
            }
            composable(NavRoutes.ATTENDANCE) {
                AttendanceScreen(viewModel = viewModel)
            }
            composable(NavRoutes.CLASSES) {
                ClassesScreen(
                    viewModel = viewModel,
                    onNavigateToClassDetail = { classId -> navController.navigate(NavRoutes.classDetail(classId)) }
                )
            }
            composable(NavRoutes.MORE) {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) }
                )
            }

            // Other Screens
            composable(NavRoutes.SAVE_EXAM) {
                SaveExamScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { newId ->
                        navController.navigate(NavRoutes.testDetail(newId)) {
                            popUpTo(NavRoutes.DASHBOARD)
                        }
                    }
                )
            }

            composable(NavRoutes.SETTINGS) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.TEST_DETAIL,
                arguments = listOf(navArgument("testId") { type = NavType.LongType })
            ) { backStackEntry ->
                val testId = backStackEntry.arguments?.getLong("testId") ?: 0L
                TestDetailScreen(
                    testId = testId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTestEditor = { id -> navController.navigate(NavRoutes.testEditor(id)) },
                    onNavigateToSheetGenerator = { id -> navController.navigate(NavRoutes.sheetGenerator(id)) },
                    onNavigateToScanner = { id -> navController.navigate(NavRoutes.scanner(id)) },
                    onNavigateToResults = { id -> navController.navigate(NavRoutes.results(id)) }
                )
            }

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

            composable(
                route = NavRoutes.SHEET_GENERATOR,
                arguments = listOf(navArgument("testId") { type = NavType.LongType })
            ) { backStackEntry ->
                val testId = backStackEntry.arguments?.getLong("testId") ?: 0L
                SheetGeneratorScreen(
                    testId = testId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToScanner = { id -> navController.navigate(NavRoutes.scanner(id)) }
                )
            }

            composable(
                route = NavRoutes.SCANNER,
                arguments = listOf(navArgument("testId") { type = NavType.LongType })
            ) { backStackEntry ->
                val testId = backStackEntry.arguments?.getLong("testId") ?: 0L
                ScannerScreen(
                    testId = testId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToReview = { navController.navigate(NavRoutes.REVIEW) }
                )
            }

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

            composable(
                route = NavRoutes.RESULTS,
                arguments = listOf(navArgument("testId") { type = NavType.LongType })
            ) { backStackEntry ->
                val testId = backStackEntry.arguments?.getLong("testId") ?: 0L
                ResultsScreen(
                    testId = testId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToResultDetail = { resultId -> navController.navigate(NavRoutes.resultDetail(resultId)) },
                    onNavigateToScanner = { id -> navController.navigate(NavRoutes.scanner(id)) }
                )
            }

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
        }
    }
}
