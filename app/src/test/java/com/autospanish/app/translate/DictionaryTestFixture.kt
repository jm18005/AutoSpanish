package com.autospanish.app.translate

import com.autospanish.app.grammar.DictionaryEntry
import com.autospanish.app.grammar.glossVariants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * Loads the real on-disk dictionary.json for JVM unit tests, mirroring
 * [com.autospanish.app.grammar.AssetDictionaries]'s lookup maps without needing an Android
 * Context, so tests exercise the exact same dictionary content the app ships with.
 */
object DictionaryTestFixture {
    private val entries: List<DictionaryEntry> by lazy {
        val file = locateDictionaryFile()
        val type = object : TypeToken<List<DictionaryEntry>>() {}.type
        Gson().fromJson(file.readText(Charsets.UTF_8), type)
    }

    val bySpanish: Map<String, DictionaryEntry> by lazy { entries.associateBy { it.spanish.lowercase() } }

    private val byInflectedForm: Map<String, DictionaryEntry> by lazy {
        val map = mutableMapOf<String, DictionaryEntry>()
        for (entry in entries) {
            entry.feminineForm?.let { map.putIfAbsent(it.lowercase(), entry) }
            entry.masculineForm?.let { map.putIfAbsent(it.lowercase(), entry) }
            entry.pluralForm?.let { map.putIfAbsent(it.lowercase(), entry) }
        }
        map
    }

    private fun stripReflexiveSuffix(word: String): String? {
        if (!word.endsWith("se") || word.length <= 4) return null
        val base = word.substring(0, word.length - 2)
        return base.takeIf { it.endsWith("ar") || it.endsWith("er") || it.endsWith("ir") }
    }

    /** Mirrors [com.autospanish.app.grammar.AssetDictionaries.resolveSpanish]'s inflection fallbacks. */
    fun resolveSpanish(raw: String): DictionaryEntry? {
        val lower = raw.lowercase()
        bySpanish[lower]?.let { return it }
        byInflectedForm[lower]?.let { return it }
        stripReflexiveSuffix(lower)?.let { base -> bySpanish[base]?.let { return it } }
        if (lower.endsWith("s")) {
            val singular = lower.dropLast(1)
            bySpanish[singular]?.let { return it }
            byInflectedForm[singular]?.let { return it }
        }
        return null
    }

    val byEnglish: Map<String, List<DictionaryEntry>> by lazy {
        val map = mutableMapOf<String, MutableList<DictionaryEntry>>()
        for (entry in entries) {
            for (meaning in entry.english) {
                for (key in glossVariants(meaning)) {
                    map.getOrPut(key) { mutableListOf() }.add(entry)
                }
            }
        }
        map.mapValues { it.value.distinct() }
    }

    private fun locateDictionaryFile(): File {
        val candidates = listOf(
            File("src/main/assets/dictionary.json"),
            File("app/src/main/assets/dictionary.json"),
        )
        return candidates.firstOrNull { it.exists() }
            ?: error("dictionary.json not found relative to ${File(".").absolutePath}")
    }
}
