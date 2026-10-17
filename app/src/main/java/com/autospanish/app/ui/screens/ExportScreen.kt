package com.autospanish.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.autospanish.app.export.CsvExporter
import com.autospanish.app.export.DocxExporter
import com.autospanish.app.export.PdfExporter
import com.autospanish.app.export.QuizletExporter
import com.autospanish.app.ui.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(viewModel: AppViewModel, listId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val words by remember(listId) { viewModel.wordsForList(listId) }.collectAsState(initial = emptyList())

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        uri?.let { context.contentResolver.openOutputStream(it)?.use { out -> CsvExporter.export(words, out) } }
    }
    val quizletLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri: Uri? ->
        uri?.let { context.contentResolver.openOutputStream(it)?.use { out -> QuizletExporter.export(words, out) } }
    }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
        uri?.let { context.contentResolver.openOutputStream(it)?.use { out -> PdfExporter.export("AutoSpanish", words, out) } }
    }
    val docxLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    ) { uri: Uri? ->
        uri?.let { context.contentResolver.openOutputStream(it)?.use { out -> DocxExporter.export("AutoSpanish", words, out) } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("${words.size} words will be exported", style = MaterialTheme.typography.bodyMedium)
            Button(onClick = { csvLauncher.launch("autospanish_export.csv") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.TableChart, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Export as CSV")
            }
            Button(onClick = { quizletLauncher.launch("autospanish_quizlet_import.txt") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Style, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Export for Quizlet import (.txt)")
            }
            Button(onClick = { pdfLauncher.launch("autospanish_export.pdf") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Export as PDF")
            }
            Button(onClick = { docxLauncher.launch("autospanish_export.docx") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Export as DOCX")
            }
        }
    }
}
