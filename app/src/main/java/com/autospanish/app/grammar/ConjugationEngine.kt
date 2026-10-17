package com.autospanish.app.grammar

data class ConjugationResult(
    val isReflexive: Boolean,
    val irregularityType: String?,
    val conjugations: Map<String, List<String>>,
)

object ConjugationEngine {

    private val REFLEXIVE_PRONOUNS = listOf("me ", "te ", "se ", "nos ", "os ", "se ")

    fun conjugate(infinitive: String, dictionaries: AssetDictionaries): ConjugationResult {
        val lower = infinitive.lowercase()
        val isReflexive = lower.endsWith("se") && lower.length > 3

        dictionaries.irregularVerbs[lower]?.let { data ->
            val conjugations = mapOf(
                "present" to data.present,
                "preterite" to (data.preterite ?: emptyList()),
                "imperfect" to (data.imperfect ?: emptyList()),
                "future" to (data.future ?: emptyList()),
            )
            return ConjugationResult(isReflexive, data.type, conjugations)
        }

        val baseInfinitive = if (isReflexive) infinitive.dropLast(2) else infinitive
        val regular = generateRegular(baseInfinitive)
        val finalConjugations = if (isReflexive) {
            regular.mapValues { (_, forms) -> forms.mapIndexed { i, form -> REFLEXIVE_PRONOUNS[i] + form } }
        } else {
            regular
        }
        return ConjugationResult(isReflexive, null, finalConjugations)
    }

    private fun generateRegular(infinitive: String): Map<String, List<String>> {
        if (infinitive.length < 3) return emptyMap()
        val stem = infinitive.dropLast(2)
        val ending = infinitive.takeLast(2)

        val present = when (ending) {
            "ar" -> listOf("o", "as", "a", "amos", "áis", "an")
            "er" -> listOf("o", "es", "e", "emos", "éis", "en")
            "ir" -> listOf("o", "es", "e", "imos", "ís", "en")
            else -> return emptyMap()
        }.map { stem + it }

        val preterite = when (ending) {
            "ar" -> listOf("é", "aste", "ó", "amos", "asteis", "aron")
            else -> listOf("í", "iste", "ió", "imos", "isteis", "ieron")
        }.map { stem + it }

        val imperfect = when (ending) {
            "ar" -> listOf("aba", "abas", "aba", "ábamos", "abais", "aban")
            else -> listOf("ía", "ías", "ía", "íamos", "íais", "ían")
        }.map { stem + it }

        val future = listOf("é", "ás", "á", "emos", "éis", "án").map { infinitive + it }

        return mapOf(
            "present" to present,
            "preterite" to preterite,
            "imperfect" to imperfect,
            "future" to future,
        )
    }
}
