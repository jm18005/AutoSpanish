package com.autospanish.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.autospanish.app.data.WordEntity

/**
 * The same on-demand grammar detail (plural, conjugations, synonyms, regional variant,
 * preposition pairing, example sentence) and auto-flags row that [WordCard] shows, packaged as a
 * standalone composable so study and Learn sessions can offer it without duplicating the info logic.
 */
@Composable
fun WordDetailMenu(word: WordEntity, modifier: Modifier = Modifier) {
    var menuExpanded by remember(word.id) { mutableStateOf(false) }
    var expandedInfo by remember(word.id) { mutableStateOf<WordInfoKind?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PosBadge(word.partOfSpeech)
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Filled.Info, contentDescription = "Word details")
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                infoMenuItems(word) { kind -> expandedInfo = kind; menuExpanded = false }
            }
        }
        WordFlagsAndInfoPanel(word, expandedInfo, onDismissInfo = { expandedInfo = null })
    }
}
