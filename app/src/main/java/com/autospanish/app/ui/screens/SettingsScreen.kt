package com.autospanish.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.autospanish.app.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AppViewModel, onBack: () -> Unit, onOpenBackup: () -> Unit) {
    val context = LocalContext.current
    val darkModeOverride by viewModel.darkModeOverride.collectAsState()
    val modelsDownloaded by viewModel.modelsDownloaded.collectAsState()
    val spanishFirst by viewModel.spanishFirst.collectAsState()
    var downloading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsSection(title = "Appearance", icon = Icons.Filled.Palette) {
                DarkModeOption("Follow system", darkModeOverride == 0) { viewModel.setDarkModeOverride(0) }
                DarkModeOption("Light", darkModeOverride == 1) { viewModel.setDarkModeOverride(1) }
                DarkModeOption("Dark", darkModeOverride == 2) { viewModel.setDarkModeOverride(2) }
            }

            SettingsSection(title = "Word display", icon = Icons.Filled.Translate) {
                DarkModeOption("Spanish first", spanishFirst) { viewModel.setSpanishFirst(true) }
                DarkModeOption("English first", !spanishFirst) { viewModel.setSpanishFirst(false) }
            }

            SettingsSection(title = "Offline translation", icon = Icons.Filled.CloudDownload) {
                Text(
                    if (modelsDownloaded) "Offline Spanish/English models are downloaded." else "Offline models not downloaded yet. Requires internet once.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(
                    onClick = {
                        downloading = true
                        viewModel.downloadOfflineModels { success ->
                            downloading = false
                            Toast.makeText(context, if (success) "Models downloaded" else "Download failed — check connection", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !downloading,
                    modifier = Modifier.padding(top = 10.dp).fillMaxWidth(),
                ) { Text(if (downloading) "Downloading…" else "Download offline models") }
            }

            SettingsSection(title = "Data", icon = Icons.Filled.Save) {
                OutlinedButton(onClick = onOpenBackup, modifier = Modifier.fillMaxWidth()) { Text("Backup & Restore") }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Column(modifier = Modifier.padding(top = 10.dp), content = content)
        }
    }
}

@Composable
private fun DarkModeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().selectable(selected = selected, onClick = onClick),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}
