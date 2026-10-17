package com.autospanish.app.grammar

import com.autospanish.app.data.Gender

data class GenderResult(val gender: Gender, val article: String)

object GenderHeuristic {

    private val FEMININE_SUFFIXES = listOf("ción", "sión", "dad", "tud", "umbre")

    fun resolve(nounSingular: String): GenderResult {
        val lower = nounSingular.lowercase()

        if (FEMININE_SUFFIXES.any { lower.endsWith(it) }) {
            return GenderResult(Gender.FEMININE, "la")
        }
        if (lower.endsWith("a")) return GenderResult(Gender.FEMININE, "la")
        if (lower.endsWith("o")) return GenderResult(Gender.MASCULINE, "el")

        return GenderResult(Gender.MASCULINE, "el")
    }

    fun pluralize(nounSingular: String): String {
        val lower = nounSingular
        return when {
            lower.endsWith("z") -> lower.dropLast(1) + "ces"
            lower.endsWith("á") || lower.endsWith("é") || lower.endsWith("í") ||
                lower.endsWith("ó") || lower.endsWith("ú") -> lower + "es"
            lower.last().isVowel() -> lower + "s"
            else -> lower + "es"
        }
    }

    fun toPlural(article: String): String = when (article) {
        "el" -> "los"
        "la" -> "las"
        else -> article
    }

    private fun Char.isVowel() = this in "aeiouAEIOU"
}
