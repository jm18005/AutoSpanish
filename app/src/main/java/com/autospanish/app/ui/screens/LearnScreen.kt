package com.autospanish.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.autospanish.app.data.WordEntity
import com.autospanish.app.ui.AppViewModel
import com.autospanish.app.ui.components.WordDetailMenu

private enum class QuizStage { MULTIPLE_CHOICE, TYPED }

private const val BATCH_SIZE = 7

/**
 * Per-word progress within a single Learn session. Not persisted — [AppViewModel.recordAnswer]
 * handles long-term mastery stats. Mirrors Quizlet's Learn mode: a word starts in multiple choice;
 * once answered right there it graduates to typed for its next appearance (mixed in among other
 * words' questions via the shared queue, not asked back-to-back); a miss at either stage demotes it
 * back to multiple choice and raises the streak required to master it.
 */
private class WordProgress(val word: WordEntity) {
    var streak = 0
    var needed = 1
    var stage = QuizStage.MULTIPLE_CHOICE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(viewModel: AppViewModel, source: String, onBack: () -> Unit) {
    // Freeze the word list for the whole session: recordAnswer() below updates each word's
    // mastery stats in Room, which re-emits this Flow mid-session (and, for the "smart" weakest-
    // words source, can reorder it). Reacting to that live would yank the user to a different
    // word right after they answer. Take only the first emission and hold it steady.
    val wordsFlow = remember(source) { resolveWordSource(viewModel, source) }
    var sessionWords by remember(source) { mutableStateOf<List<WordEntity>?>(null) }
    LaunchedEffect(source) {
        wordsFlow.collect { emitted ->
            if (sessionWords == null) sessionWords = emitted
        }
    }
    val words = sessionWords
    val spanishFirst by viewModel.spanishFirst.collectAsState()
    val lists by viewModel.lists.collectAsState()

    // A word leaves the queue once its streak of consecutive correct answers reaches `needed`.
    // Getting a word wrong bumps its `needed` to 2, so it must then be answered right twice in a
    // row (not just once) before it's considered learned for this session.
    // At most BATCH_SIZE words are in play at once (`queue`); the rest wait in `pool` and are
    // introduced one at a time as words graduate out, rather than starting every word at once.
    var progressList by remember(source) { mutableStateOf<List<WordProgress>?>(null) }
    var queue by remember(source) { mutableStateOf<List<Int>>(emptyList()) }
    var pool by remember(source) { mutableStateOf<List<Int>>(emptyList()) }
    LaunchedEffect(words) {
        words?.let {
            progressList = it.map { w -> WordProgress(w) }
            val shuffled = it.indices.shuffled()
            queue = shuffled.take(BATCH_SIZE)
            pool = shuffled.drop(BATCH_SIZE)
        }
    }

    var correctCount by remember(source) { mutableStateOf(0) }
    var totalCount by remember(source) { mutableStateOf(0) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var pendingCorrect by remember { mutableStateOf(false) }
    var typedAnswer by remember { mutableStateOf("") }
    var mcOptions by remember { mutableStateOf(listOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sourceTitle(source, lists)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            )
        },
    ) { padding ->
        val progress = progressList
        if (words == null || progress == null) {
            return@Scaffold
        }
        if (words.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.Center) {
                Text("Nothing to learn here yet.", modifier = Modifier.fillMaxWidth().padding(24.dp))
            }
            return@Scaffold
        }

        val currentIdx = queue.firstOrNull()
        if (currentIdx == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Session complete", style = MaterialTheme.typography.titleLarge)
                Text(
                    "$correctCount / $totalCount correct",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("Done") }
            }
            return@Scaffold
        }

        val wordProgress = progress[currentIdx]
        val word = wordProgress.word
        val format = wordProgress.stage
        val prompt = if (spanishFirst) word.spanish.first() else word.englishMeanings.first()
        // A word can have several comma-separated accepted forms (englishMeanings, or the
        // Spanish alternates the user entered). Multiple choice always tests against just the
        // primary form, but a typed answer must give every form, in any order, to count as known.
        val requiredAnswers = if (spanishFirst) word.englishMeanings else word.spanish
        val expectedAnswer = requiredAnswers.joinToString(", ")
        val isExpectedSingle = { candidate: String -> requiredAnswers.first().equals(candidate, ignoreCase = true) }
        val isExpectedTyped = { candidate: String ->
            val given = candidate.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
            given == requiredAnswers.map { it.trim().lowercase() }.toSet()
        }

        fun finalizeTurn(correct: Boolean) {
            viewModel.recordAnswer(word, correct)
            totalCount++
            if (correct) correctCount++
            if (!correct) {
                wordProgress.stage = QuizStage.MULTIPLE_CHOICE
                wordProgress.needed = 2
                wordProgress.streak = 0
            } else if (wordProgress.stage == QuizStage.MULTIPLE_CHOICE) {
                wordProgress.stage = QuizStage.TYPED
            } else {
                wordProgress.streak++
            }
            val rest = queue.drop(1)
            queue = if (wordProgress.streak >= wordProgress.needed) {
                if (pool.isNotEmpty()) {
                    val next = pool.first()
                    pool = pool.drop(1)
                    rest + next
                } else {
                    rest
                }
            } else {
                rest + currentIdx
            }
            feedback = null
            pendingCorrect = false
            typedAnswer = ""
        }

        LaunchedEffect(currentIdx) {
            mcOptions = buildOptions(word, words, spanishFirst)
            feedback = null
            typedAnswer = ""
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            val remaining = queue.size + pool.size
            LinearProgressIndicator(
                progress = { 1f - remaining.toFloat() / words.size },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                if (format == QuizStage.MULTIPLE_CHOICE) "Multiple choice" else "Type the translation",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = if (spanishFirst && word.article != null) "${word.article} $prompt" else prompt,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 16.dp),
            )
            WordDetailMenu(word, modifier = Modifier.padding(bottom = 8.dp))

            if (feedback == null) {
                if (format == QuizStage.MULTIPLE_CHOICE) {
                    mcOptions.forEach { option ->
                        OutlinedButton(
                            onClick = {
                                val correct = isExpectedSingle(option)
                                feedback = if (correct) "✓ Correct" else "✗ Correct answer: $expectedAnswer"
                                pendingCorrect = correct
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        ) { Text(option) }
                    }
                } else {
                    OutlinedTextField(
                        value = typedAnswer,
                        onValueChange = { typedAnswer = it },
                        label = { Text(if (requiredAnswers.size > 1) "Translation (comma-separated, any order)" else "Translation") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = {
                            val correct = isExpectedTyped(typedAnswer.trim())
                            feedback = if (correct) "✓ Correct" else "✗ Correct answer: $expectedAnswer"
                            pendingCorrect = correct
                        },
                        modifier = Modifier.padding(top = 12.dp),
                        enabled = typedAnswer.isNotBlank(),
                    ) { Text("Submit") }
                }
            }

            feedback?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(top = 12.dp),
                    color = if (pendingCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 12.dp)) {
                    // Typed answers can be marked wrong by a typo or an acceptable answer the
                    // dictionary doesn't list; let the user override rather than force a redo.
                    if (!pendingCorrect && format == QuizStage.TYPED) {
                        OutlinedButton(onClick = {
                            feedback = "✓ Correct"
                            pendingCorrect = true
                        }) { Text("I was right") }
                    }
                    Button(onClick = { finalizeTurn(pendingCorrect) }) { Text("Continue") }
                }
            }
        }
    }
}

private fun buildOptions(word: WordEntity, allWords: List<WordEntity>, spanishFirst: Boolean): List<String> {
    val correct = if (spanishFirst) word.englishMeanings.first() else word.spanish.first()
    val distractorPool = allWords.filter { it.id != word.id }
        .mapNotNull { if (spanishFirst) it.englishMeanings.firstOrNull() else it.spanish.firstOrNull() }
        .distinct()
    val distractors = distractorPool.shuffled().take(3)
    return (listOf(correct) + distractors).shuffled()
}
