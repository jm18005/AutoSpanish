package com.autospanish.app.grammar

import com.autospanish.app.data.Gender
import com.autospanish.app.data.PartOfSpeech

data class EnrichedGrammar(
    val gender: Gender = Gender.NONE,
    val article: String? = null,
    val plural: String? = null,
    val counterpartForm: String? = null,
    val isReflexive: Boolean = false,
    val irregularityType: String? = null,
    val conjugations: Map<String, List<String>> = emptyMap(),
    val serEstarHint: String? = null,
    val falseFriendWarning: String? = null,
    val synonyms: List<String> = emptyList(),
    val regionalNote: String? = null,
    val prepositionPairing: String? = null,
)

object GrammarEnricher {

    fun enrich(
        spanish: String,
        englishMeanings: List<String>,
        pos: PartOfSpeech,
        dictionaries: AssetDictionaries,
        professionEntry: DictionaryEntry?,
    ): EnrichedGrammar {
        val falseFriend = dictionaries.falseFriends[spanish.lowercase()]?.let {
            "Looks like English \"${it.lookLikeEnglish}\" but means \"${it.actualMeaning}\". (${it.note})"
        }
        val regionalNote = dictionaries.regionalVariants[spanish.lowercase()]?.let {
            "In Spain, often \"${it.spain}\" instead. ${it.note}"
        }
        // Prefer the source dictionary's own synonyms/antonyms; only derive synonyms by
        // reverse-gloss lookup when the source didn't record any.
        val sourcedSynonyms = professionEntry?.synonyms
        val synonyms = if (!sourcedSynonyms.isNullOrEmpty()) {
            sourcedSynonyms
        } else {
            findSynonyms(spanish, englishMeanings, pos, dictionaries)
        }

        return when (pos) {
            PartOfSpeech.NOUN -> enrichNoun(spanish, professionEntry, falseFriend, regionalNote, synonyms)
            PartOfSpeech.ADJECTIVE -> enrichAdjective(spanish, dictionaries, professionEntry, falseFriend, regionalNote, synonyms)
            PartOfSpeech.VERB -> enrichVerb(spanish, dictionaries, professionEntry, falseFriend, regionalNote, synonyms)
            else -> EnrichedGrammar(falseFriendWarning = falseFriend, regionalNote = regionalNote, synonyms = synonyms)
        }
    }

    private fun enrichNoun(
        spanish: String,
        professionEntry: DictionaryEntry?,
        falseFriend: String?,
        regionalNote: String?,
        synonyms: List<String>,
    ): EnrichedGrammar {
        if (professionEntry != null && professionEntry.profession) {
            val plural = professionEntry.pluralForm ?: GenderHeuristic.pluralize(spanish)
            return when {
                professionEntry.invariable -> EnrichedGrammar(
                    gender = Gender.NONE,
                    article = "el/la",
                    plural = plural,
                    falseFriendWarning = falseFriend,
                    regionalNote = regionalNote,
                    synonyms = synonyms,
                )
                professionEntry.feminineForm != null -> EnrichedGrammar(
                    gender = Gender.MASCULINE,
                    article = "el",
                    plural = plural,
                    counterpartForm = professionEntry.feminineForm,
                    falseFriendWarning = falseFriend,
                    regionalNote = regionalNote,
                    synonyms = synonyms,
                )
                professionEntry.masculineForm != null -> EnrichedGrammar(
                    gender = Gender.FEMININE,
                    article = "la",
                    plural = plural,
                    counterpartForm = professionEntry.masculineForm,
                    falseFriendWarning = falseFriend,
                    regionalNote = regionalNote,
                    synonyms = synonyms,
                )
                else -> enrichPlainNoun(spanish, professionEntry, falseFriend, regionalNote, synonyms)
            }
        }
        return enrichPlainNoun(spanish, professionEntry, falseFriend, regionalNote, synonyms)
    }

    private fun enrichPlainNoun(
        spanish: String,
        dictEntry: DictionaryEntry?,
        falseFriend: String?,
        regionalNote: String?,
        synonyms: List<String>,
    ): EnrichedGrammar {
        // Prefer the gender/plural the source dictionary states directly; only fall back to the
        // suffix heuristic when the dictionary didn't record it.
        val genderResult = when (dictEntry?.gender) {
            "m" -> GenderResult(Gender.MASCULINE, "el")
            "f" -> GenderResult(Gender.FEMININE, "la")
            else -> GenderHeuristic.resolve(spanish)
        }
        return EnrichedGrammar(
            gender = genderResult.gender,
            article = genderResult.article,
            plural = dictEntry?.pluralForm ?: GenderHeuristic.pluralize(spanish),
            falseFriendWarning = falseFriend,
            regionalNote = regionalNote,
            synonyms = synonyms,
        )
    }

    private fun enrichAdjective(
        spanish: String,
        dictionaries: AssetDictionaries,
        dictEntry: DictionaryEntry?,
        falseFriend: String?,
        regionalNote: String?,
        synonyms: List<String>,
    ): EnrichedGrammar {
        val serEstar = dictionaries.serEstarAdjectives[spanish.lowercase()]?.let {
            "ser = ${it.ser}  /  estar = ${it.estar}"
        }
        // Prefer the source dictionary's own invariable flag / feminine form; only fall back to
        // the suffix heuristic (and its small hand-curated exception list) when the source lacks it.
        val counterpart = when {
            dictEntry?.invariable == true -> null
            dictEntry?.feminineForm != null -> dictEntry.feminineForm.takeIf { it != spanish }
            else -> AdjectiveFormGenerator.resolve(spanish).let { if (it.feminine != it.masculine) it.feminine else null }
        }
        return EnrichedGrammar(
            counterpartForm = counterpart,
            serEstarHint = serEstar,
            falseFriendWarning = falseFriend,
            regionalNote = regionalNote,
            synonyms = synonyms,
        )
    }

    private fun enrichVerb(
        spanish: String,
        dictionaries: AssetDictionaries,
        dictEntry: DictionaryEntry?,
        falseFriend: String?,
        regionalNote: String?,
        synonyms: List<String>,
    ): EnrichedGrammar {
        val baseForLookup: String
        val isReflexive: Boolean
        val irregularityType: String?
        val conjugations: Map<String, List<String>>

        val sourced = dictEntry?.conjugationTable
        if (!sourced.isNullOrEmpty()) {
            isReflexive = spanish.lowercase().endsWith("se") && spanish.length > 3
            baseForLookup = if (isReflexive) spanish.dropLast(2) else spanish
            irregularityType = null
            conjugations = sourced
        } else {
            val conjResult = ConjugationEngine.conjugate(spanish, dictionaries)
            isReflexive = conjResult.isReflexive
            baseForLookup = if (isReflexive) spanish.dropLast(2) else spanish
            irregularityType = conjResult.irregularityType
            conjugations = conjResult.conjugations
        }

        val prepPairing = dictionaries.prepositionPairings[baseForLookup.lowercase()]?.let {
            "${it.verb} ${it.preposition} — ${it.meaning}"
        }
        return EnrichedGrammar(
            isReflexive = isReflexive,
            irregularityType = irregularityType,
            conjugations = conjugations,
            falseFriendWarning = falseFriend,
            regionalNote = regionalNote,
            synonyms = synonyms,
            prepositionPairing = prepPairing,
        )
    }

    private fun findSynonyms(
        spanish: String,
        englishMeanings: List<String>,
        pos: PartOfSpeech,
        dictionaries: AssetDictionaries,
    ): List<String> {
        val results = mutableSetOf<String>()
        for (meaning in englishMeanings) {
            val candidates = dictionaries.dictionaryByEnglish[meaning.lowercase()] ?: continue
            for (candidate in candidates) {
                if (candidate.pos == pos.name && !candidate.spanish.equals(spanish, ignoreCase = true)) {
                    results.add(candidate.spanish)
                }
            }
        }
        return results.take(5).toList()
    }
}
