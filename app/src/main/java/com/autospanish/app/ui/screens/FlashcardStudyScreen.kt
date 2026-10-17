package com.autospanish.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.autospanish.app.data.SortMode
import com.autospanish.app.ui.AppViewModel
import com.autospanish.app.ui.components.WordDetailMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardStudyScreen(viewModel: AppViewModel, source: String, onBack: () -> Unit) {
    val rawWords by remember(source) { resolveWordSource(viewModel, source) }.collectAsState(initial = emptyList())
    val spanishFirst by viewModel.spanishFirst.collectAsState()
    val lists by viewModel.lists.collectAsState()
    var sortMode by remember { mutableStateOf(SortMode.RECENT) }
    val words = remember(rawWords, sortMode) { sortWords(rawWords, sortMode) }

    var index by remember(words.size) { mutableStateOf(0) }
    var flipped by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sourceTitle(source, lists)) },
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
                },
            )
        },
    ) { padding ->
        if (words.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center) {
                Text("Nothing to study here yet.", modifier = Modifier.fillMaxWidth().padding(24.dp))
            }
            return@Scaffold
        }

        val word = words[index.coerceIn(0, words.size - 1)]
        val rotation by animateFloatAsState(targetValue = if (flipped) 180f else 0f, label = "flip")

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LinearProgressIndicator(
                progress = { (index + 1).toFloat() / words.size },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "${index + 1} / ${words.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.4f)
                    .graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .clickable { flipped = !flipped },
                contentAlignment = Alignment.Center,
            ) {
                val spanishJoined = word.spanish.joinToString(", ")
                val spanishText = if (word.article != null) "${word.article} $spanishJoined" else spanishJoined
                val englishText = word.englishMeanings.joinToString("; ")
                val front = if (spanishFirst) spanishText else englishText
                val back = if (spanishFirst) englishText else spanishText
                if (rotation <= 90f) {
                    Text(text = front, style = MaterialTheme.typography.titleLarge)
                } else {
                    Text(
                        text = back,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            WordDetailMenu(word)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        flipped = false
                        index = if (index == 0) words.size - 1 else index - 1
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Previous")
                }
                Button(
                    onClick = {
                        flipped = false
                        index = (index + 1) % words.size
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Next")
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
