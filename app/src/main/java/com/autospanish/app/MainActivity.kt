package com.autospanish.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.autospanish.app.ui.AppViewModel
import com.autospanish.app.ui.navigation.AutoSpanishNavGraph
import com.autospanish.app.ui.theme.AutoSpanishTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels {
        val app = application as AutoSpanishApp
        AppViewModel.Factory(app.repository, app.backupManager, app.settingsDataStore, app.mlKitTranslateService)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkModeOverride by viewModel.darkModeOverride.collectAsState()
            AutoSpanishTheme(darkModeOverride = darkModeOverride) {
                AutoSpanishNavGraph(viewModel = viewModel)
            }
        }
    }
}
