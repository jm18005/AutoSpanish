package com.autospanish.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autospanish.app.data.PartOfSpeech
import com.autospanish.app.data.WordEntity
import com.autospanish.app.grammar.GenderHeuristic

enum class WordInfoKind { PLURAL, CONJUGATIONS, SYNONYMS, REGIONAL, PREPOSITION, EXAMPLE }

@Composable
fun WordCard(
    word: WordEntity,
    onAddToList: () -> Unit,
    onRemoveFromHere: (() -> Unit)?,
    onDeleteGlobally: () -> Unit,
    onEdit: (WordEntity) -> Unit,
    modifier: Modifier = Modifier,
    spanishFirst: Boolean = true,
    selectionMode: Boolean = false,
    selected: Boolean = false,
    onToggleSelect: () -> Unit = {},
) {
    var menuExpanded by remember(word.id) { mutableStateOf(false) }
    var expandedInfo by remember(word.id) { mutableStateOf<WordInfoKind?>(null) }
    var showEditDialog by remember(word.id) { mutableStateOf(false) }

    Card(
        modifier = if (selectionMode) modifier.fillMaxWidth().clickable { onToggleSelect() } else modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                if (selectionMode) {
                    Checkbox(checked = selected, onCheckedChange = { onToggleSelect() })
                } else {
                    PosBadge(word.partOfSpeech)
                }
                androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    val primary = if (spanishFirst) spanishHeadline(word) else word.englishMeanings.joinToString("; ")
                    val secondary = if (spanishFirst) word.englishMeanings.joinToString("; ") else spanishHeadline(word)
                    Text(
                        text = primary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = secondary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!selectionMode) {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        infoMenuItems(word) { kind -> expandedInfo = kind; menuExpanded = false }
                        DropdownMenuItem(text = { Text("Add to list") }, onClick = { onAddToList(); menuExpanded = false })
                        if (onRemoveFromHere != null) {
                            DropdownMenuItem(text = { Text("Remove from here") }, onClick = { onRemoveFromHere(); menuExpanded = false })
                        }
                        DropdownMenuItem(text = { Text("Edit") }, onClick = { showEditDialog = true; menuExpanded = false })
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { onDeleteGlobally(); menuExpanded = false },
                        )
                    }
                }
            }

            WordFlagsAndInfoPanel(word, expandedInfo, onDismissInfo = { expandedInfo = null })
        }
    }

    if (showEditDialog) {
        EditWordDialog(
            word = word,
            onDismiss = { showEditDialog = false },
            onSave = { spanishForms, englishMeanings ->
                onEdit(word.copy(spanish = spanishForms, englishMeanings = englishMeanings))
                showEditDialog = false
            },
        )
    }
}

/** Shared "show plural / conjugations / synonyms / ..." menu items, reused by [WordCard] and [WordDetailMenu]. */
@Composable
internal fun infoMenuItems(word: WordEntity, onPick: (WordInfoKind) -> Unit) {
    if (word.plural != null) {
        DropdownMenuItem(text = { Text("Show plural") }, onClick = { onPick(WordInfoKind.PLURAL) })
    }
    if (word.conjugations.isNotEmpty()) {
        DropdownMenuItem(text = { Text("Show conjugations") }, onClick = { onPick(WordInfoKind.CONJUGATIONS) })
    }
    if (word.synonyms.isNotEmpty()) {
        DropdownMenuItem(text = { Text("Show synonyms") }, onClick = { onPick(WordInfoKind.SYNONYMS) })
    }
    if (word.regionalNote != null) {
        DropdownMenuItem(text = { Text("Show regional variant") }, onClick = { onPick(WordInfoKind.REGIONAL) })
    }
    if (word.prepositionPairing != null) {
        DropdownMenuItem(text = { Text("Show preposition pairing") }, onClick = { onPick(WordInfoKind.PREPOSITION) })
    }
    if (word.exampleSentences.isNotEmpty()) {
        DropdownMenuItem(text = { Text("Show example sentence") }, onClick = { onPick(WordInfoKind.EXAMPLE) })
    }
}

/** Auto-flags row (irregularity, reflexive, ser/estar, false friend, counterpart) plus the expanded info panel. */
@Composable
internal fun WordFlagsAndInfoPanel(word: WordEntity, expandedInfo: WordInfoKind?, onDismissInfo: () -> Unit) {
    val autoFlags = buildAutoFlags(word)
    if (autoFlags.isNotEmpty() || word.timesTried > 0) {
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (word.timesTried > 0) MasteryChip(word.masteryPercent)
            autoFlags.forEach { flag -> FlagChip(flag.first, flag.second) }
        }
    }

    expandedInfo?.let { kind ->
        Column(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                .padding(10.dp)
                .clickable { onDismissInfo() },
        ) {
            Text(text = infoText(word, kind), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun spanishHeadline(word: WordEntity): String {
    val prefix = if (word.article != null) "${word.article} " else ""
    return "$prefix${word.spanish.joinToString(", ")}"
}

@Composable
internal fun PosBadge(pos: PartOfSpeech) {
    val label = when (pos) {
        PartOfSpeech.NOUN -> "noun/sustantivo"
        PartOfSpeech.VERB -> "verb/verbo"
        PartOfSpeech.ADJECTIVE -> "adjective/adjetivo"
        PartOfSpeech.ADVERB -> "adverb/adverbio"
        PartOfSpeech.OTHER -> "phrase/frase"
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .wrapContentWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}

@Composable
private fun MasteryChip(percent: Int) {
    val color = when {
        percent >= 80 -> MaterialTheme.colorScheme.tertiary
        percent >= 50 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }
    Text(
        text = "$percent% mastered",
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}

@Composable
private fun FlagChip(text: String, warning: Boolean) {
    val color = if (warning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}

private fun buildAutoFlags(word: WordEntity): List<Pair<String, Boolean>> {
    val flags = mutableListOf<Pair<String, Boolean>>()
    if (word.irregularityType != null) flags.add("Irregular: ${word.irregularityType}" to false)
    if (word.isReflexive) flags.add("Reflexive" to false)
    if (word.serEstarHint != null) flags.add(word.serEstarHint to false)
    if (word.falseFriendWarning != null) flags.add("False friend" to true)
    if (word.counterpartForm != null) flags.add("Also: ${word.counterpartForm}" to false)
    return flags
}

private fun infoText(word: WordEntity, kind: WordInfoKind): String = when (kind) {
    WordInfoKind.PLURAL -> {
        val pluralArticle = word.article?.let { GenderHeuristic.toPlural(it) }
        if (pluralArticle != null) "Plural: $pluralArticle ${word.plural}" else "Plural: ${word.plural}"
    }
    WordInfoKind.CONJUGATIONS -> word.conjugations.entries.joinToString("\n\n") { (tense, forms) ->
        "${tense.replaceFirstChar { it.uppercase() }}: ${forms.joinToString(", ")}"
    }
    WordInfoKind.SYNONYMS -> "Synonyms: ${word.synonyms.joinToString(", ")}"
    WordInfoKind.REGIONAL -> word.regionalNote ?: ""
    WordInfoKind.PREPOSITION -> word.prepositionPairing ?: ""
    WordInfoKind.EXAMPLE -> word.exampleSentences.joinToString("\n")
}
