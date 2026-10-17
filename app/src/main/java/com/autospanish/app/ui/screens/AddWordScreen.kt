package com.autospanish.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.autospanish.app.data.TranslationDirection
import com.autospanish.app.translate.rememberIsOnline
import com.autospanish.app.ui.AppViewModel
import com.autospanish.app.ui.components.PickerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordScreen(
    viewModel: AppViewModel,
    initialTargetListId: Long,
    onBack: () -> Unit,
    onOcrCapture: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val online by rememberIsOnline()
    val lists by viewModel.lists.collectAsState()
    var tabIndex by remember { mutableStateOf(0) }
    var direction by remember { mutableStateOf(TranslationDirection.AUTO) }
    var targetListId by remember { mutableStateOf(initialTargetListId) }
    var showTargetPicker by remember { mutableStateOf(false) }
    var pendingAmbiguousWord by remember { mutableStateOf<String?>(null) }

    var manualInput by remember { mutableStateOf("") }
    var lastSubmittedInput by remember { mutableStateOf("") }
    var paragraphInput by remember { mutableStateOf("") }
    var candidateWords by remember { mutableStateOf(listOf<String>()) }
    var selectedWords by remember { mutableStateOf(setOf<String>()) }
    var isSubmitting by remember { mutableStateOf(false) }
    var isBulkAdding by remember { mutableStateOf(false) }

    val lastAddResult by viewModel.lastAddResult.collectAsState()
    val targetListName = lists.find { it.id == targetListId }?.name ?: "All Words"

    LaunchedEffect(lastAddResult) {
        lastAddResult?.let { result ->
            val message = when (result) {
                is com.autospanish.app.data.AddWordResult.Added -> "Added \"$lastSubmittedInput\""
                is com.autospanish.app.data.AddWordResult.AlreadyExists -> "\"$lastSubmittedInput\" was already in your words — linked here"
                is com.autospanish.app.data.AddWordResult.TranslationFailed -> "Couldn't translate that. Try being online, or check spelling."
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearLastAddResult()
            isSubmitting = false
            if (result is com.autospanish.app.data.AddWordResult.Added || result is com.autospanish.app.data.AddWordResult.AlreadyExists) {
                manualInput = ""
            }
        }
    }

    fun submitManualInput() {
        val input = manualInput.trim()
        if (input.isEmpty()) return
        lastSubmittedInput = input
        isSubmitting = true
        if (direction == TranslationDirection.AUTO && !input.contains(" ")) {
            scope.launch {
                val resolved = viewModel.resolveDirection(input)
                if (resolved == null) {
                    pendingAmbiguousWord = input
                    isSubmitting = false
                } else {
                    viewModel.addWord(input, resolved, online, targetListId)
                }
            }
        } else {
            viewModel.addWord(input, direction, online, targetListId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add word") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = onOcrCapture) { Icon(Icons.Filled.CameraAlt, contentDescription = "Scan text") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (!online) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                    Text(
                        "Offline — using on-device dictionary and downloaded models only.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Add to: $targetListName",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { showTargetPicker = true }) { Text("Change") }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DirectionOption("Auto", direction == TranslationDirection.AUTO) { direction = TranslationDirection.AUTO }
                        DirectionOption("ES → EN", direction == TranslationDirection.ES_TO_EN) { direction = TranslationDirection.ES_TO_EN }
                        DirectionOption("EN → ES", direction == TranslationDirection.EN_TO_ES) { direction = TranslationDirection.EN_TO_ES }
                    }
                }
            }

            TabRow(selectedTabIndex = tabIndex) {
                Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Manual") })
                Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Paste paragraph") })
            }

            if (tabIndex == 0) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it },
                        label = { Text("Word, phrase, or sentence") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Button(
                        onClick = { submitManualInput() },
                        enabled = manualInput.isNotBlank() && !isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp).padding(end = 8.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Text("Translating…")
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                            Text("Translate & Add")
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = paragraphInput,
                        onValueChange = { paragraphInput = it },
                        label = { Text("Paste a paragraph") },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                    )
                    OutlinedButton(
                        onClick = {
                            candidateWords = tokenize(paragraphInput)
                            selectedWords = emptySet()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Extract words") }

                    if (candidateWords.isNotEmpty()) {
                        Text("Tap the words you want to add:", style = MaterialTheme.typography.bodyMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(candidateWords) { word ->
                                FilterChip(
                                    selected = word in selectedWords,
                                    onClick = {
                                        selectedWords = if (word in selectedWords) selectedWords - word else selectedWords + word
                                    },
                                    label = { Text(word) },
                                )
                            }
                        }
                        Button(
                            onClick = {
                                isBulkAdding = true
                                viewModel.addWords(selectedWords.toList(), direction, online, targetListId) { added, dup, failed ->
                                    isBulkAdding = false
                                    candidateWords = emptyList()
                                    selectedWords = emptySet()
                                    paragraphInput = ""
                                    Toast.makeText(context, "Added $added, linked $dup existing, $failed failed", Toast.LENGTH_LONG).show()
                                }
                            },
                            enabled = selectedWords.isNotEmpty() && !isBulkAdding,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            if (isBulkAdding) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp).padding(end = 8.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                                Text("Adding…")
                            } else {
                                Text("Add ${selectedWords.size} selected")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTargetPicker) {
        PickerDialog(
            title = "Add to list",
            options = lists.map { it.id to it.name },
            onDismiss = { showTargetPicker = false },
            onPick = { id -> targetListId = id; showTargetPicker = false },
            onCreateNew = { name ->
                viewModel.createList(name) { newId -> targetListId = newId }
                showTargetPicker = false
            },
        )
    }

    pendingAmbiguousWord?.let { word ->
        AlertDialog(
            onDismissRequest = { pendingAmbiguousWord = null },
            title = { Text("Spanish or English?") },
            text = { Text("Is \"$word\" a Spanish word or an English word?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.addWord(word, TranslationDirection.ES_TO_EN, online, targetListId)
                    pendingAmbiguousWord = null
                }) { Text("Spanish") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.addWord(word, TranslationDirection.EN_TO_ES, online, targetListId)
                    pendingAmbiguousWord = null
                }) { Text("English") }
            },
        )
    }
}

@Composable
private fun DirectionOption(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}

private fun tokenize(paragraph: String): List<String> =
    Regex("[\\p{L}]+").findAll(paragraph)
        .map { it.value }
        .filter { it.length > 1 }
        .distinctBy { it.lowercase() }
        .toList()
