package com.autospanish.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.autospanish.app.data.WordEntity

@Composable
fun EditWordDialog(
    word: WordEntity,
    onDismiss: () -> Unit,
    onSave: (spanishForms: List<String>, englishMeanings: List<String>) -> Unit,
) {
    var spanish by remember(word.id) { mutableStateOf(word.spanish.joinToString(", ")) }
    var english by remember(word.id) { mutableStateOf(word.englishMeanings.joinToString(", ")) }

    val spanishForms = spanish.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val meanings = english.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val canSave = spanishForms.isNotEmpty() && meanings.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit word") },
        text = {
            Column {
                OutlinedTextField(
                    value = spanish,
                    onValueChange = { spanish = it },
                    label = { Text("Spanish (comma-separated)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = english,
                    onValueChange = { english = it },
                    label = { Text("English (comma-separated)") },
                    modifier = androidx.compose.ui.Modifier.padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(spanishForms, meanings) }, enabled = canSave) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
