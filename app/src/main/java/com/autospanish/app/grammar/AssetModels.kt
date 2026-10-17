package com.autospanish.app.grammar

data class DictionaryEntry(
    val spanish: String,
    val english: List<String>,
    val pos: String,
    val profession: Boolean = false,
    val feminineForm: String? = null,
    val masculineForm: String? = null,
    val invariable: Boolean = false,
    /** "m" or "f" when the source dictionary states the noun's gender directly; null to fall back to heuristics. */
    val gender: String? = null,
    /** Sourced plural form; null to fall back to the suffix heuristic. */
    val pluralForm: String? = null,
    /** Sourced conjugation table keyed by tense/mood (e.g. "present", "subjunctive_imperfect", "imperative"),
     * each value an ordered list of person forms; null to fall back to [ConjugationEngine]'s rule-based generation. */
    val conjugationTable: Map<String, List<String>>? = null,
    /** Real usage examples from the source dictionary, each optionally suffixed with " — <translation>". */
    val exampleSentences: List<String>? = null,
    val synonyms: List<String>? = null,
    val antonyms: List<String>? = null,
)

data class IrregularVerbData(
    val type: String,
    val tenses: List<String>,
    val present: List<String>,
    val preterite: List<String>? = null,
    val imperfect: List<String>? = null,
    val future: List<String>? = null,
    val english: List<String>,
)

data class SerEstarEntry(val word: String, val ser: String, val estar: String)

data class FalseFriendEntry(
    val spanish: String,
    val lookLikeEnglish: String,
    val actualMeaning: String,
    val note: String,
)

data class PrepositionPairingEntry(val verb: String, val preposition: String, val meaning: String)

data class RegionalVariantEntry(val latam: String, val spain: String, val note: String)
