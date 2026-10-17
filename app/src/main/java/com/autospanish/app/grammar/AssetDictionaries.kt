package com.autospanish.app.grammar

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

/** Strips a leading "to " infinitive marker, e.g. "to eat" -> "eat"; otherwise returns the input unchanged. */
fun stripToPrefix(gloss: String): String {
    val trimmed = gloss.trim()
    return if (trimmed.startsWith("to ", ignoreCase = true)) trimmed.substring(3) else trimmed
}

/** Strips a trailing "?", "!", or "." — some glosses mark an interrogative/exclamatory sense that way. */
private fun stripTrailingPunctuation(gloss: String): String = gloss.trim().trimEnd('?', '!', '.').trim()

private val ALTERNATIVE_SEPARATOR = Regex("""\s+/\s+|\s+\bor\b\s+""")

/**
 * Expands a dictionary gloss into every lowercase form a user's query might match: the gloss
 * itself, its infinitive stripped of a leading "to ", its trailing "?"/"!"/"." stripped, and —
 * for compound glosses like "grey / gray" or "to plunge or sink into" — each alternative
 * phrasing on its own, with the same stripping applied. Purely additive: it only adds extra
 * lookup keys pointing at the same entry.
 */
fun glossVariants(gloss: String): Set<String> {
    val trimmed = gloss.trim()
    val parts = trimmed.split(ALTERNATIVE_SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }
    return (listOf(trimmed) + parts)
        .flatMap { listOf(it, stripToPrefix(it), stripTrailingPunctuation(it)) }
        .map { it.lowercase() }
        .toSet()
}

/** Strips a Spanish reflexive infinitive suffix, e.g. "casarse" -> "casar"; otherwise null. */
private fun stripReflexiveSuffix(word: String): String? {
    if (!word.endsWith("se") || word.length <= 4) return null
    val base = word.substring(0, word.length - 2)
    return base.takeIf { it.endsWith("ar") || it.endsWith("er") || it.endsWith("ir") }
}

class AssetDictionaries private constructor(context: Context) {
    private val gson = Gson()
    private val appContext = context.applicationContext

    private fun <T> load(fileName: String, type: java.lang.reflect.Type): T {
        appContext.assets.open(fileName).use { stream ->
            InputStreamReader(stream, Charsets.UTF_8).use { reader ->
                return gson.fromJson(reader, type)
            }
        }
    }

    val dictionaryBySpanish: Map<String, DictionaryEntry> by lazy {
        val list: List<DictionaryEntry> = load("dictionary.json", object : TypeToken<List<DictionaryEntry>>() {}.type)
        list.associateBy { it.spanish.lowercase() }
    }

    /** Feminine/masculine/plural inflected spellings the source only recorded as a field on the base entry. */
    private val dictionaryByInflectedForm: Map<String, DictionaryEntry> by lazy {
        val list: List<DictionaryEntry> = load("dictionary.json", object : TypeToken<List<DictionaryEntry>>() {}.type)
        val map = mutableMapOf<String, DictionaryEntry>()
        for (entry in list) {
            entry.feminineForm?.let { map.putIfAbsent(it.lowercase(), entry) }
            entry.masculineForm?.let { map.putIfAbsent(it.lowercase(), entry) }
            entry.pluralForm?.let { map.putIfAbsent(it.lowercase(), entry) }
        }
        map
    }

    /**
     * Looks up a Spanish word the user typed, falling back past the dictionary's exact spelling
     * when the word is a common inflection the source only recorded on a related entry: a
     * feminine/masculine/plural form stored as a field on the base spelling (e.g. "embarazada" on
     * "embarazado"), a reflexive infinitive missing its "-se" (e.g. "casarse" -> "casar"), or a
     * regular plural missing its "-s" (e.g. "juntos" -> "junto").
     */
    fun resolveSpanish(raw: String): DictionaryEntry? {
        val lower = raw.lowercase()
        dictionaryBySpanish[lower]?.let { return it }
        dictionaryByInflectedForm[lower]?.let { return it }
        stripReflexiveSuffix(lower)?.let { base -> dictionaryBySpanish[base]?.let { return it } }
        if (lower.endsWith("s")) {
            val singular = lower.dropLast(1)
            dictionaryBySpanish[singular]?.let { return it }
            dictionaryByInflectedForm[singular]?.let { return it }
        }
        return null
    }

    val dictionaryByEnglish: Map<String, List<DictionaryEntry>> by lazy {
        val list: List<DictionaryEntry> = load("dictionary.json", object : TypeToken<List<DictionaryEntry>>() {}.type)
        val map = mutableMapOf<String, MutableList<DictionaryEntry>>()
        for (entry in list) {
            for (meaning in entry.english) {
                for (key in glossVariants(meaning)) {
                    map.getOrPut(key) { mutableListOf() }.add(entry)
                }
            }
        }
        map.mapValues { it.value.distinct() }
    }

    val irregularVerbs: Map<String, IrregularVerbData> by lazy {
        load("irregular_verbs.json", object : TypeToken<Map<String, IrregularVerbData>>() {}.type)
    }

    val serEstarAdjectives: Map<String, SerEstarEntry> by lazy {
        val list: List<SerEstarEntry> = load("ser_estar_adjectives.json", object : TypeToken<List<SerEstarEntry>>() {}.type)
        list.associateBy { it.word.lowercase() }
    }

    val falseFriends: Map<String, FalseFriendEntry> by lazy {
        val list: List<FalseFriendEntry> = load("false_friends.json", object : TypeToken<List<FalseFriendEntry>>() {}.type)
        list.associateBy { it.spanish.lowercase() }
    }

    val prepositionPairings: Map<String, PrepositionPairingEntry> by lazy {
        val list: List<PrepositionPairingEntry> = load("preposition_pairings.json", object : TypeToken<List<PrepositionPairingEntry>>() {}.type)
        list.associateBy { it.verb.lowercase() }
    }

    val regionalVariants: Map<String, RegionalVariantEntry> by lazy {
        val list: List<RegionalVariantEntry> = load("regional_variants.json", object : TypeToken<List<RegionalVariantEntry>>() {}.type)
        list.associateBy { it.latam.lowercase() }
    }

    companion object {
        @Volatile private var instance: AssetDictionaries? = null

        fun getInstance(context: Context): AssetDictionaries =
            instance ?: synchronized(this) {
                instance ?: AssetDictionaries(context).also { instance = it }
            }
    }
}
