package com.autospanish.app.ui.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.autospanish.app.data.GLOBAL_LIST_ID
import com.autospanish.app.ui.AppViewModel
import com.autospanish.app.ui.UndoAction
import com.autospanish.app.ui.screens.AddWordScreen
import com.autospanish.app.ui.screens.BackupScreen
import com.autospanish.app.ui.screens.ExportScreen
import com.autospanish.app.ui.screens.FlashcardStudyScreen
import com.autospanish.app.ui.screens.HomeScreen
import com.autospanish.app.ui.screens.LearnScreen
import com.autospanish.app.ui.screens.ListDetailScreen
import com.autospanish.app.ui.screens.OcrCaptureScreen
import com.autospanish.app.ui.screens.SearchScreen
import com.autospanish.app.ui.screens.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoSpanishNavGraph(viewModel: AppViewModel) {
    val navController: NavHostController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val pendingUndo by viewModel.pendingUndo.collectAsState()

    LaunchedEffect(pendingUndo) {
        val action = pendingUndo ?: return@LaunchedEffect
        val message = when (action) {
            is UndoAction.WordDeleted -> "Deleted \"${action.word.spanish.joinToString(", ")}\""
            is UndoAction.ListDeleted -> "Deleted \"${action.list.name}\""
        }
        val result = snackbarHostState.showSnackbar(message, actionLabel = "Undo")
        if (result == SnackbarResult.ActionPerformed) viewModel.undo() else viewModel.clearUndo()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        NavHost(navController = navController, startDestination = Destinations.HOME) {
            composable(Destinations.HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onOpenList = { navController.navigate(Destinations.listDetail(it)) },
                    onOpenSearch = { navController.navigate(Destinations.SEARCH) },
                    onOpenSettings = { navController.navigate(Destinations.SETTINGS) },
                    onQuickAdd = { navController.navigate(Destinations.addWord(GLOBAL_LIST_ID)) },
                    onStudySmart = { navController.navigate(Destinations.study("smart")) },
                    onLearnSmart = { navController.navigate(Destinations.learn("smart")) },
                )
            }

            composable(
                Destinations.LIST_DETAIL,
                arguments = listOf(navArgument("listId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getLong("listId") ?: return@composable
                ListDetailScreen(
                    viewModel = viewModel,
                    listId = listId,
                    onBack = { navController.popBackStack() },
                    onAddWord = { navController.navigate(Destinations.addWord(listId)) },
                    onOcrCapture = { navController.navigate(Destinations.ocrCapture(listId)) },
                    onExport = { navController.navigate(Destinations.export(listId)) },
                    onStudy = { navController.navigate(Destinations.study("list_$listId")) },
                    onLearn = { navController.navigate(Destinations.learn("list_$listId")) },
                )
            }

            composable(
                Destinations.ADD_WORD,
                arguments = listOf(navArgument("targetListId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val targetListId = backStackEntry.arguments?.getLong("targetListId") ?: GLOBAL_LIST_ID
                AddWordScreen(
                    viewModel = viewModel,
                    initialTargetListId = targetListId,
                    onBack = { navController.popBackStack() },
                    onOcrCapture = { navController.navigate(Destinations.ocrCapture(targetListId)) },
                )
            }

            composable(
                Destinations.OCR_CAPTURE,
                arguments = listOf(navArgument("targetListId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val targetListId = backStackEntry.arguments?.getLong("targetListId") ?: GLOBAL_LIST_ID
                OcrCaptureScreen(
                    viewModel = viewModel,
                    targetListId = targetListId,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                Destinations.STUDY,
                arguments = listOf(navArgument("source") { type = NavType.StringType }),
            ) { backStackEntry ->
                val source = backStackEntry.arguments?.getString("source") ?: "smart"
                FlashcardStudyScreen(viewModel = viewModel, source = source, onBack = { navController.popBackStack() })
            }

            composable(
                Destinations.LEARN,
                arguments = listOf(navArgument("source") { type = NavType.StringType }),
            ) { backStackEntry ->
                val source = backStackEntry.arguments?.getString("source") ?: "smart"
                LearnScreen(viewModel = viewModel, source = source, onBack = { navController.popBackStack() })
            }

            composable(Destinations.SEARCH) {
                SearchScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }

            composable(
                Destinations.EXPORT,
                arguments = listOf(navArgument("listId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getLong("listId") ?: GLOBAL_LIST_ID
                ExportScreen(viewModel = viewModel, listId = listId, onBack = { navController.popBackStack() })
            }

            composable(Destinations.BACKUP) {
                BackupScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }

            composable(Destinations.SETTINGS) {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenBackup = { navController.navigate(Destinations.BACKUP) },
                )
            }
        }
    }
}
