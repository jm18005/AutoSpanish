package com.autospanish.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autospanish.app.data.WordEntity
import com.autospanish.app.data.WordListEntity

/**
 * Lets the user search and multi-select existing words and/or other lists to merge into the
 * current list, in one dialog. Excludes the current list itself from the "Lists" tab.
 */
@Composable
fun AddToListDialog(
    currentListId: Long,
    words: List<WordEntity>,
    lists: List<WordListEntity>,
    onDismiss: () -> Unit,
    onConfirm: (selectedWordIds: Set<Long>, selectedListIds: Set<Long>) -> Unit,
) {
    var tabIndex by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var selectedWordIds by remember { mutableStateOf(setOf<Long>()) }
    var selectedListIds by remember { mutableStateOf(setOf<Long>()) }

    val otherLists = remember(lists, currentListId) { lists.filter { it.id != currentListId && !it.isGlobal } }
    val filteredWords = remember(words, query) {
        if (query.isBlank()) words else words.filter {
            it.spanish.any { s -> s.contains(query, ignoreCase = true) } ||
                it.englishMeanings.any { m -> m.contains(query, ignoreCase = true) }
        }
    }
    val filteredLists = remember(otherLists, query) {
        if (query.isBlank()) otherLists else otherLists.filter { it.name.contains(query, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to this list") },
        text = {
            Column {
                TabRow(selectedTabIndex = tabIndex) {
                    Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Words") })
                    Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Other lists") })
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    singleLine = true,
                )
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp)) {
                    if (tabIndex == 0) {
                        items(filteredWords, key = { it.id }) { word ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedWordIds = if (word.id in selectedWordIds) {
                                            selectedWordIds - word.id
                                        } else {
                                            selectedWordIds + word.id
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(checked = word.id in selectedWordIds, onCheckedChange = null)
                                Column(Modifier.padding(start = 8.dp)) {
                                    Text(word.spanish.joinToString(", "))
                                    Text(word.englishMeanings.joinToString("; "))
                                }
                            }
                        }
                    } else {
                        items(filteredLists, key = { it.id }) { list ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedListIds = if (list.id in selectedListIds) {
                                            selectedListIds - list.id
                                        } else {
                                            selectedListIds + list.id
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(checked = list.id in selectedListIds, onCheckedChange = null)
                                Text(list.name, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedWordIds, selectedListIds) },
                enabled = selectedWordIds.isNotEmpty() || selectedListIds.isNotEmpty(),
            ) {
                Text("Add selected")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
