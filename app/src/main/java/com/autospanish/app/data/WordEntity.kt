package com.autospanish.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val spanish: List<String>,
    val englishMeanings: List<String>,
    val partOfSpeech: PartOfSpeech,
    val gender: Gender = Gender.NONE,
    val article: String? = null,
    val plural: String? = null,
    val isReflexive: Boolean = false,
    val counterpartForm: String? = null,
    val irregularityType: String? = null,
    val conjugations: Map<String, List<String>> = emptyMap(),
    val serEstarHint: String? = null,
    val falseFriendWarning: String? = null,
    val synonyms: List<String> = emptyList(),
    val regionalNote: String? = null,
    val exampleSentences: List<String> = emptyList(),
    val prepositionPairing: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val timesCorrect: Int = 0,
    val timesTried: Int = 0,
) {
    val masteryPercent: Int
        get() = if (timesTried == 0) 0 else (timesCorrect * 100) / timesTried
}
