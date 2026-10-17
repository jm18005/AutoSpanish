package com.autospanish.app.ui.screens

import com.autospanish.app.data.SortMode
import com.autospanish.app.data.WordEntity
import com.autospanish.app.data.WordListEntity
import com.autospanish.app.ui.AppViewModel
import kotlinx.coroutines.flow.Flow

fun resolveWordSource(viewModel: AppViewModel, source: String): Flow<List<WordEntity>> = when {
    source == "smart" -> viewModel.weakestWords()
    source.startsWith("list_") -> viewModel.wordsForList(source.removePrefix("list_").toLong())
    else -> viewModel.wordsForList(source.toLongOrNull() ?: 1L)
}

fun sourceTitle(source: String, lists: List<WordListEntity>): String = when {
    source == "smart" -> "Smart Review"
    source.startsWith("list_") -> {
        val id = source.removePrefix("list_").toLongOrNull()
        lists.find { it.id == id }?.name ?: "List"
    }
    else -> lists.find { it.id == source.toLongOrNull() }?.name ?: "List"
}

fun sortWords(words: List<WordEntity>, mode: SortMode): List<WordEntity> = when (mode) {
    SortMode.RECENT -> words.sortedByDescending { it.dateAdded }
    SortMode.OLDEST -> words.sortedBy { it.dateAdded }
    SortMode.RANDOM -> words.shuffled()
    SortMode.ALPHABETICAL_ES -> words.sortedBy { it.spanish.firstOrNull()?.lowercase() ?: "" }
    SortMode.ALPHABETICAL_EN -> words.sortedBy { it.englishMeanings.firstOrNull()?.lowercase() ?: "" }
}
