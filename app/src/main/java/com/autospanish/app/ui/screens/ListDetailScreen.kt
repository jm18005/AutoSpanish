package com.autospanish.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.autospanish.app.data.SortMode
import com.autospanish.app.data.WordEntity
import com.autospanish.app.ui.AppViewModel
import com.autospanish.app.ui.components.AddToListDialog
import com.autospanish.app.ui.components.ConfirmDialog
import com.autospanish.app.ui.components.PickerDialog
import com.autospanish.app.ui.components.WordCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    viewModel: AppViewModel,
    listId: Long,
    onBack: () -> Unit,
    onAddWord: () -> Unit,
    onOcrCapture: () -> Unit,
    onExport: () -> Unit,
    onStudy: () -> Unit,
    onLearn: () -> Unit,
) {
    val lists by viewModel.lists.collectAsState()
    val allWords by viewModel.allWords.collectAsState()
    val spanishFirst by viewModel.spanishFirst.collectAsState()
    val list = lists.find { it.id == listId }
    val rawWords by remember(listId) { viewModel.wordsForList(listId) }.collectAsState(initial = emptyList())

    var sortMode by remember { mutableStateOf(SortMode.RECENT) }
    val words = remember(rawWords, sortMode) { sortWords(rawWords, sortMode) }
    var filterQuery by remember(listId) { mutableStateOf("") }
    val displayedWords = remember(words, filterQuery) {
        if (filterQuery.isBlank()) {
            words
        } else {
            words.filter {
                it.spanish.any { s -> s.contains(filterQuery, ignoreCase = true) } ||
                    it.englishMeanings.any { m -> m.contains(filterQuery, ignoreCase = true) }
            }
        }
    }

    var menuExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showAddToListDialog by remember { mutableStateOf(false) }
    var wordPendingListPick by remember { mutableStateOf<WordEntity?>(null) }
    var selectionMode by remember(listId) { mutableStateOf(false) }
    var selectedIds by remember(listId) { mutableStateOf(setOf<Long>()) }
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
                                selectedIds = if (selectedIds.size == displayedWords.size) {
                                    emptySet()
                                } else {
                                    displayedWords.map { it.id }.toSet()
                                }
                            },
                        ) { Icon(Icons.Filled.SelectAll, contentDescription = "Select all") }
                        IconButton(
                            onClick = { showBulkListPicker = true },
                            enabled = selectedIds.isNotEmpty(),
                        ) { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Add to another list") }
                        if (list?.isGlobal == false) {
                            IconButton(
                                onClick = {
                                    selectedIds.forEach { viewModel.unlinkWordFromList(it, listId) }
                                    exitSelectionMode()
                                },
                                enabled = selectedIds.isNotEmpty(),
                            ) { Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Remove from this list") }
                        }
                        IconButton(
                            onClick = { showBulkDeleteConfirm = true },
                            enabled = selectedIds.isNotEmpty(),
                        ) { Icon(Icons.Filled.Delete, contentDescription = "Delete permanently") }
                    },
                )
            } else {
                TopAppBar(
                    title = { Text(list?.name ?: "") },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                    actions = {
                        IconButton(onClick = { sortMenuExpanded = true }) { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort") }
                        DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                            DropdownMenuItem(text = { Text("Most recent") }, onClick = { sortMode = SortMode.RECENT; sortMenuExpanded = false })
                            DropdownMenuItem(text = { Text("Oldest") }, onClick = { sortMode = SortMode.OLDEST; sortMenuExpanded = false })
                            DropdownMenuItem(text = { Text("Random") }, onClick = { sortMode = SortMode.RANDOM; sortMenuExpanded = false })
                            DropdownMenuItem(text = { Text("A-Z (Spanish)") }, onClick = { sortMode = SortMode.ALPHABETICAL_ES; sortMenuExpanded = false })
                            DropdownMenuItem(text = { Text("A-Z (English)") }, onClick = { sortMode = SortMode.ALPHABETICAL_EN; sortMenuExpanded = false })
                        }
                        IconButton(onClick = onOcrCapture) { Icon(Icons.Filled.CameraAlt, contentDescription = "Scan text") }
                        IconButton(onClick = onAddWord) { Icon(Icons.Filled.Add, contentDescription = "Add word") }
                        IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "More") }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            if (words.isNotEmpty()) {
                                DropdownMenuItem(text = { Text("Select words") }, onClick = { selectionMode = true; menuExpanded = false })
                            }
                            if (list?.isGlobal == false) {
                                DropdownMenuItem(text = { Text("Rename list") }, onClick = { showRenameDialog = true; menuExpanded = false })
                                DropdownMenuItem(
                                    text = { Text("Delete list") },
                                    onClick = { list.let { viewModel.deleteList(it) }; menuExpanded = false; onBack() },
                                )
                            }
                            DropdownMenuItem(text = { Text("Add to this list") }, onClick = {
                                showAddToListDialog = true; menuExpanded = false
                            })
                            DropdownMenuItem(text = { Text("Export") }, onClick = { onExport(); menuExpanded = false })
                        }
                    },
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (words.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(onClick = onStudy, modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Icon(Icons.Filled.Style, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Study")
                    }
                    OutlinedButton(onClick = onLearn, modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Learn")
                    }
                }
            }

            if (words.size > 8) {
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = { Text("Filter this list") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (filterQuery.isNotEmpty()) {
                            IconButton(onClick = { filterQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear filter")
                            }
                        }
                    },
                )
            }

            if (words.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                ) {
                    Text(
                        "No words yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Tap + to add one, or use the camera to scan text.",
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            } else if (displayedWords.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                ) {
                    Text(
                        "No words match \"$filterQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(displayedWords, key = { it.id }) { word ->
                        WordCard(
                            word = word,
                            onAddToList = { wordPendingListPick = word },
                            onRemoveFromHere = if (list?.isGlobal == false) {
                                { viewModel.unlinkWordFromList(word.id, listId) }
                            } else null,
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
    }

    if (showRenameDialog && list != null) {
        RenameDialog(
            currentName = list.name,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName -> viewModel.renameList(list, newName); showRenameDialog = false },
        )
    }

    if (showAddToListDialog) {
        AddToListDialog(
            currentListId = listId,
            words = allWords,
            lists = lists,
            onDismiss = { showAddToListDialog = false },
            onConfirm = { selectedWordIds, selectedListIds ->
                selectedWordIds.forEach { viewModel.linkWordToList(it, listId) }
                selectedListIds.forEach { viewModel.mergeListIntoList(it, listId) }
                showAddToListDialog = false
            },
        )
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
                selectedIds.forEach { id -> words.find { it.id == id }?.let(viewModel::deleteWordGlobally) }
                exitSelectionMode()
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenameDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(currentName) }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename") },
        text = {
            androidx.compose.material3.OutlinedTextField(value = text, onValueChange = { text = it })
        },
        confirmButton = {
            androidx.compose.material3.Button(onClick = { onConfirm(text.trim()) }, enabled = text.isNotBlank()) { Text("Save") }
        },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
