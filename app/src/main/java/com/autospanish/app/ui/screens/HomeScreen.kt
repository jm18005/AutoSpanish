package com.autospanish.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autospanish.app.data.WordListEntity
import com.autospanish.app.ui.AppViewModel
import com.autospanish.app.ui.components.PickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    onOpenList: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onQuickAdd: () -> Unit,
    onStudySmart: () -> Unit,
    onLearnSmart: () -> Unit,
) {
    val lists by viewModel.lists.collectAsState()
    var showNewListDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AutoSpanish", style = MaterialTheme.typography.headlineSmall) },
                actions = {
                    IconButton(onClick = onOpenSearch) { Icon(Icons.Filled.Search, contentDescription = "Search") }
                    IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onQuickAdd,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Add word") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { SmartReviewCard(onStudySmart, onLearnSmart) }

            item {
                SectionHeader(title = "Lists", onAddClick = { showNewListDialog = true })
            }

            if (lists.isEmpty()) {
                item { EmptyListsState() }
            } else {
                itemsIndexed(lists, key = { _, list -> list.id }) { index, list ->
                    ListCard(
                        list = list,
                        viewModel = viewModel,
                        onClick = { onOpenList(list.id) },
                        onMoveUp = if (!list.isGlobal && index > 1) {
                            { viewModel.reorderLists(lists.swapped(index, index - 1)) }
                        } else null,
                        onMoveDown = if (!list.isGlobal && index < lists.size - 1) {
                            { viewModel.reorderLists(lists.swapped(index, index + 1)) }
                        } else null,
                    )
                }
            }
        }
    }

    if (showNewListDialog) {
        PickerDialog(
            title = "New list",
            options = emptyList(),
            onDismiss = { showNewListDialog = false },
            onPick = {},
            onCreateNew = { name -> viewModel.createList(name); showNewListDialog = false },
        )
    }
}

@Composable
private fun SmartReviewCard(onStudySmart: () -> Unit, onLearnSmart: () -> Unit) {
    Card(
        onClick = onLearnSmart,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Column(modifier = Modifier.padding(start = 14.dp).weight(1f)) {
                Text(
                    "Smart Review",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    "Focus on your weakest words",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
                Row(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onStudySmart) {
                        Icon(Icons.Filled.Style, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(" Study", modifier = Modifier.padding(start = 4.dp))
                    }
                    TextButton(onClick = onLearnSmart) {
                        Text("Learn")
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp).padding(start = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onAddClick) { Icon(Icons.Filled.Add, contentDescription = "New $title") }
    }
}

@Composable
private fun EmptyListsState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "No lists yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Tap + to create your first list",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

private fun List<WordListEntity>.swapped(i: Int, j: Int): List<WordListEntity> {
    val mutable = toMutableList()
    val tmp = mutable[i]
    mutable[i] = mutable[j]
    mutable[j] = tmp
    return mutable
}

@Composable
private fun ListCard(
    list: WordListEntity,
    viewModel: AppViewModel,
    onClick: () -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
) {
    val wordCount by remember(list.id) { viewModel.wordCountForList(list.id) }.collectAsState(initial = 0)
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (list.isGlobal) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = list.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (wordCount == 1) "1 word" else "$wordCount words",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (onMoveUp != null || onMoveDown != null) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Reorder list")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Move up") },
                            onClick = { onMoveUp?.invoke(); menuExpanded = false },
                            enabled = onMoveUp != null,
                        )
                        DropdownMenuItem(
                            text = { Text("Move down") },
                            onClick = { onMoveDown?.invoke(); menuExpanded = false },
                            enabled = onMoveDown != null,
                        )
                    }
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
