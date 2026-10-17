package com.autospanish.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.autospanish.app.ui.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }
    var isBusy by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        uri?.let {
            isBusy = true
            scope.launch {
                context.contentResolver.openOutputStream(it)?.use { out -> viewModel.exportBackup(out) }
                isBusy = false
                Toast.makeText(context, "Backup saved", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        pendingRestoreUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Back up all your words, lists, and sets to a JSON file you control.", style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = { exportLauncher.launch("autospanish_backup.json") },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp).padding(end = 8.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                }
                Text(if (isBusy) "Working…" else "Export backup")
            }
            OutlinedButton(
                onClick = { restoreLauncher.launch(arrayOf("application/json")) },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Restore, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Restore from backup")
            }
        }
    }

    pendingRestoreUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            title = { Text("Restore backup") },
            text = { Text("Merge with your current data, or replace everything with the backup?") },
            confirmButton = {
                TextButton(onClick = {
                    isBusy = true
                    scope.launch {
                        context.contentResolver.openInputStream(uri)?.use { viewModel.restoreBackup(it, merge = true) }
                        isBusy = false
                        Toast.makeText(context, "Merged backup into your data", Toast.LENGTH_SHORT).show()
                    }
                    pendingRestoreUri = null
                }) { Text("Merge") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isBusy = true
                        scope.launch {
                            context.contentResolver.openInputStream(uri)?.use { viewModel.restoreBackup(it, merge = false) }
                            isBusy = false
                            Toast.makeText(context, "Replaced all data from backup", Toast.LENGTH_SHORT).show()
                        }
                        pendingRestoreUri = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Replace all") }
            },
        )
    }
}
