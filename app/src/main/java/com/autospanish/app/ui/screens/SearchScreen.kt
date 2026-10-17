package com.autospanish.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.autospanish.app.data.WordEntity
import com.autospanish.app.ui.AppViewModel
import com.autospanish.app.ui.components.ConfirmDialog
import com.autospanish.app.ui.components.PickerDialog
import com.autospanish.app.ui.components.WordCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val lists by viewModel.lists.collectAsState()
    val spanishFirst by viewModel.spanishFirst.collectAsState()
    val results by remember(query) {
        if (query.isBlank()) kotlinx.coroutines.flow.flowOf(emptyList()) else viewModel.searchWords(query)
    }.collectAsState(initial = emptyList())

    var wordPendingListPick by remember { mutableStateOf<WordEntity?>(null) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    var showBulkListPicker by remember { mutableStateOf(false) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }

    fun exitSelectionMode() {
        selectionMode = false
        selectedIds = emptySet()
    }

    Scaffold(
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    title = { Text("${selectedIds.size} selected") },
                    navigationIcon = { IconButton(onClick = ::exitSelectionMode) { Icon(Icons.Filled.Close, null) } },
                    actions = {
                        IconButton(
                            onClick = {
                                selectedIds = if (selectedIds.size == results.size) emptySet() else results.map { it.id }.toSet()
                            },
                        ) { Icon(Icons.Filled.SelectAll, contentDescription = "Select all") }
                        IconButton(
                            onClick = { showBulkListPicker = true },
                            enabled = selectedIds.isNotEmpty(),
                        ) { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Add to list") }
                        IconButton(
                            onClick = { showBulkDeleteConfirm = true },
                            enabled = selectedIds.isNotEmpty(),
                        ) { Icon(Icons.Filled.Delete, contentDescription = "Delete permanently") }
                    },
                )
            } else {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Search words…") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { query = "" }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Clear search")
                                    }
                                }
                            },
                        )
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                    actions = {
                        if (results.isNotEmpty()) {
                            IconButton(onClick = { selectionMode = true }) { Icon(Icons.Filled.Checklist, contentDescription = "Select") }
                        }
                    },
                )
            }
        },
    ) { padding ->
        if (results.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    if (query.isBlank()) "Start typing to search your words" else "No words match \"$query\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(results, key = { it.id }) { word ->
                    WordCard(
                        word = word,
                        onAddToList = { wordPendingListPick = word },
                        onRemoveFromHere = null,
                        onDeleteGlobally = { viewModel.deleteWordGlobally(word) },
                        onEdit = { viewModel.updateWord(it) },
                        spanishFirst = spanishFirst,
                        selectionMode = selectionMode,
                        selected = word.id in selectedIds,
                        onToggleSelect = {
                            selectedIds = if (word.id in selectedIds) selectedIds - word.id else selectedIds + word.id
                        },
                    )
                }
            }
        }
    }

    wordPendingListPick?.let { word ->
        PickerDialog(
            title = "Add to list",
            options = lists.map { it.id to it.name },
            onDismiss = { wordPendingListPick = null },
            onPick = { id -> viewModel.linkWordToList(word.id, id); wordPendingListPick = null },
            onCreateNew = { name ->
                viewModel.createList(name) { newId -> viewModel.linkWordToList(word.id, newId) }
                wordPendingListPick = null
            },
        )
    }

    if (showBulkListPicker) {
        PickerDialog(
            title = "Add ${selectedIds.size} words to list",
            options = lists.map { it.id to it.name },
            onDismiss = { showBulkListPicker = false },
            onPick = { id ->
                selectedIds.forEach { viewModel.linkWordToList(it, id) }
                showBulkListPicker = false
                exitSelectionMode()
            },
            onCreateNew = { name ->
                viewModel.createList(name) { newId -> selectedIds.forEach { viewModel.linkWordToList(it, newId) } }
                showBulkListPicker = false
                exitSelectionMode()
            },
        )
    }

    if (showBulkDeleteConfirm) {
        ConfirmDialog(
            title = "Delete ${selectedIds.size} words?",
            text = "These words will be permanently deleted from all lists. This can't be undone.",
            onDismiss = { showBulkDeleteConfirm = false },
            onConfirm = {
                selectedIds.forEach { id -> results.find { it.id == id }?.let(viewModel::deleteWordGlobally) }
                exitSelectionMode()
            },
        )
    }
}
