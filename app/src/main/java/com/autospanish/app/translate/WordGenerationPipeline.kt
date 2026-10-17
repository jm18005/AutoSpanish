package com.autospanish.app.translate

import com.autospanish.app.data.Gender
import com.autospanish.app.data.PartOfSpeech
import com.autospanish.app.data.TranslationDirection
import com.autospanish.app.grammar.AssetDictionaries
import com.autospanish.app.grammar.DictionaryEntry
import com.autospanish.app.grammar.EnrichedGrammar
import com.autospanish.app.grammar.ExampleSentenceGenerator
import com.autospanish.app.grammar.GrammarEnricher

data class GeneratedWord(
    val spanish: String,
    val englishMeanings: List<String>,
    val partOfSpeech: PartOfSpeech,
    val gender: Gender,
    val article: String?,
    val plural: String?,
    val counterpartForm: String?,
    val isReflexive: Boolean,
    val irregularityType: String?,
    val conjugations: Map<String, List<String>>,
    val serEstarHint: String?,
    val falseFriendWarning: String?,
    val synonyms: List<String>,
    val regionalNote: String?,
    val exampleSentences: List<String>,
    val prepositionPairing: String?,
)

sealed class TranslationOutcome {
    data class Success(val word: GeneratedWord) : TranslationOutcome()
    object Failed : TranslationOutcome()
}

class WordGenerationPipeline(
    private val dictionaries: AssetDictionaries,
    private val mlKit: MlKitTranslateService,
) {

    suspend fun generate(rawInput: String, direction: TranslationDirection, allowOnline: Boolean): TranslationOutcome {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return TranslationOutcome.Failed
        return if (trimmed.contains(" ")) {
            generatePhrase(trimmed, direction, allowOnline)
        } else {
            generateWord(trimmed, direction, allowOnline)
        }
    }

    /** Builds the final [GeneratedWord] for a resolved Spanish entry, folding any other-sense candidates in as synonyms. */
    private fun finishFromEntry(entry: DictionaryEntry, requestedEnglish: String, extraSynonyms: List<String> = emptyList()): GeneratedWord {
        val base = finishGeneration(entry.spanish, listOf(requestedEnglish.trim()), PartOfSpeech.valueOf(entry.pos), entry)
        return if (extraSynonyms.isEmpty()) base else base.copy(synonyms = (base.synonyms + extraSynonyms).distinct())
    }

    private suspend fun generateWord(raw: String, direction: TranslationDirection, allowOnline: Boolean): TranslationOutcome {
        val resolvedDirection = if (direction == TranslationDirection.AUTO) detectDirection(raw) else direction

        if (resolvedDirection == TranslationDirection.ES_TO_EN) {
            val dictEntry = dictionaries.resolveSpanish(raw)
            if (dictEntry != null) {
                return TranslationOutcome.Success(
                    finishGeneration(dictEntry.spanish, dictEntry.english, PartOfSpeech.valueOf(dictEntry.pos), dictEntry)
                )
            }
            val meanings = mutableListOf<String>()
            mlKit.translateEsToEn(raw)?.let { if (!isNoOpTranslation(raw, it)) meanings.add(it) }
            if (allowOnline) {
                meanings.addAll(MyMemoryClient.translateAndCollectMeanings(raw, "es|en").filterNot { isNoOpTranslation(raw, it) })
            }
            val distinct = meanings.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }
            if (distinct.isEmpty()) return TranslationOutcome.Failed
            val pos = guessPartOfSpeech(raw, multiWord = false)
            return TranslationOutcome.Success(finishGeneration(raw, distinct, pos, professionEntry = null))
        }

        // Multiple Spanish words can share an English gloss (e.g. "professor" also glosses
        // "catedrático", "mother" also glosses "mai"). The less common word usually lists the
        // common one as its own synonym (catedrático -> profesor, mai -> madre), so those collapse
        // to one sense. Candidates with no such link (e.g. "orange" the fruit vs. the color) are
        // genuinely different meanings — ask the user rather than silently guessing one.
        val dictMatches = dictionaries.dictionaryByEnglish[raw.lowercase()]
        if (!dictMatches.isNullOrEmpty()) {
            val resolution = SenseResolver.resolve(raw, dictMatches)
            return TranslationOutcome.Success(finishFromEntry(resolution.entry, raw, resolution.extraSynonyms))
        }

        var candidateSpanish = mlKit.translateEnToEs(raw)?.takeUnless { isNoOpTranslation(raw, it) }
        if (candidateSpanish == null && allowOnline) {
            candidateSpanish = MyMemoryClient.translateAndCollectMeanings(raw, "en|es")
                .firstOrNull { !isNoOpTranslation(raw, it) }
        }
        if (candidateSpanish == null) return TranslationOutcome.Failed
        val spanish = candidateSpanish
        val pos = guessPartOfSpeech(spanish, multiWord = spanish.trim().contains(" "))
        return TranslationOutcome.Success(finishGeneration(spanish, listOf(raw), pos, professionEntry = null))
    }

    private fun finishGeneration(
        spanish: String,
        englishMeanings: List<String>,
        pos: PartOfSpeech,
        professionEntry: DictionaryEntry?,
    ): GeneratedWord {
        val grammar: EnrichedGrammar = GrammarEnricher.enrich(spanish, englishMeanings, pos, dictionaries, professionEntry)
        val examples = ExampleSentenceGenerator.generate(professionEntry)

        return GeneratedWord(
            spanish = spanish,
            englishMeanings = englishMeanings,
            partOfSpeech = pos,
            gender = grammar.gender,
            article = grammar.article,
            plural = grammar.plural,
            counterpartForm = grammar.counterpartForm,
            isReflexive = grammar.isReflexive,
            irregularityType = grammar.irregularityType,
            conjugations = grammar.conjugations,
            serEstarHint = grammar.serEstarHint,
            falseFriendWarning = grammar.falseFriendWarning,
            synonyms = grammar.synonyms,
            regionalNote = grammar.regionalNote,
            exampleSentences = examples,
            prepositionPairing = grammar.prepositionPairing,
        )
    }

    private suspend fun generatePhrase(raw: String, direction: TranslationDirection, allowOnline: Boolean): TranslationOutcome {
        val resolvedDirection = if (direction == TranslationDirection.AUTO) detectDirection(raw.split(" ").first()) else direction

        val spanish: String
        val englishMeanings: List<String>

        if (resolvedDirection == TranslationDirection.ES_TO_EN) {
            var translated = mlKit.translateEsToEn(raw)?.takeUnless { isNoOpTranslation(raw, it) }
            if (translated == null && allowOnline) {
                translated = MyMemoryClient.translateAndCollectMeanings(raw, "es|en").firstOrNull { !isNoOpTranslation(raw, it) }
            }
            if (translated == null) return TranslationOutcome.Failed
            spanish = raw
            englishMeanings = listOf(translated)
        } else {
            var translated = mlKit.translateEnToEs(raw)?.takeUnless { isNoOpTranslation(raw, it) }
            if (translated == null && allowOnline) {
                translated = MyMemoryClient.translateAndCollectMeanings(raw, "en|es").firstOrNull { !isNoOpTranslation(raw, it) }
            }
            if (translated == null) return TranslationOutcome.Failed
            spanish = translated
            englishMeanings = listOf(raw)
        }

        return TranslationOutcome.Success(
            GeneratedWord(
                spanish = spanish,
                englishMeanings = englishMeanings,
                partOfSpeech = PartOfSpeech.OTHER,
                gender = Gender.NONE,
                article = null,
                plural = null,
                counterpartForm = null,
                isReflexive = false,
                irregularityType = null,
                conjugations = emptyMap(),
                serEstarHint = null,
                falseFriendWarning = null,
                synonyms = emptyList(),
                regionalNote = null,
                exampleSentences = emptyList(),
                prepositionPairing = null,
            )
        )
    }

    private val SPANISH_ENDINGS = listOf(
        "ción", "sión", "dad", "tud", "umbre", "mente", "ando", "iendo",
        "amos", "emos", "imos", "aba", "ía", "ará", "erá", "irá",
    )

    /**
     * Returns the confidently-detected direction for a single word, or null when the word is
     * genuinely ambiguous — either a valid entry in both the Spanish and English dictionaries at
     * once (e.g. "red": English for the color, or Spanish for "net"), or unrecognized in either
     * dictionary with no confident language signal. Callers should ask the user to disambiguate in
     * that case rather than guess. Words missing from the dictionary (e.g. "why") aren't guessed by
     * default — an on-device language identifier decides them, so "why" resolves to English on its
     * own merits instead of via a blind fallback.
     */
    suspend fun resolveDirectionOrNull(rawInput: String): TranslationDirection? {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty() || trimmed.contains(" ")) return null
        val lower = trimmed.lowercase()
        val inSpanish = dictionaries.dictionaryBySpanish.containsKey(lower)
        val inEnglish = dictionaries.dictionaryByEnglish.containsKey(lower)
        if (inSpanish && inEnglish) return null
        if (inSpanish) return TranslationDirection.ES_TO_EN
        if (inEnglish) return TranslationDirection.EN_TO_ES
        if (lower.any { it in "áéíóúñü¿¡" }) return TranslationDirection.ES_TO_EN
        if (SPANISH_ENDINGS.any { lower.endsWith(it) }) return TranslationDirection.ES_TO_EN
        return when (mlKit.identifyLanguage(lower)) {
            "es" -> TranslationDirection.ES_TO_EN
            "en" -> TranslationDirection.EN_TO_ES
            else -> null
        }
    }

    private suspend fun detectDirection(word: String): TranslationDirection =
        resolveDirectionOrNull(word) ?: TranslationDirection.EN_TO_ES

    /**
     * ML Kit's on-device model has no "I don't recognize this word" signal — it always emits
     * something, and for input it can't translate that's usually just the input echoed back
     * unchanged. MyMemory does the same for out-of-vocabulary queries. Treat an echoed result as
     * a failed lookup so gibberish surfaces as "couldn't translate" instead of being added verbatim.
     */
    private fun isNoOpTranslation(input: String, candidate: String): Boolean =
        candidate.trim().equals(input.trim(), ignoreCase = true)

    private fun guessPartOfSpeech(spanish: String, multiWord: Boolean): PartOfSpeech {
        if (multiWord) return PartOfSpeech.OTHER
        val lower = spanish.lowercase()
        return when {
            lower.endsWith("mente") -> PartOfSpeech.ADVERB
            lower.length > 2 && (lower.endsWith("ar") || lower.endsWith("er") || lower.endsWith("ir")) -> PartOfSpeech.VERB
            lower.endsWith("oso") || lower.endsWith("osa") || lower.endsWith("ico") || lower.endsWith("ica") ||
                lower.endsWith("ble") || lower.endsWith("ivo") || lower.endsWith("iva") -> PartOfSpeech.ADJECTIVE
            lower.endsWith("o") || lower.endsWith("a") || SPANISH_ENDINGS.any { lower.endsWith(it) } -> PartOfSpeech.NOUN
            else -> PartOfSpeech.OTHER
        }
    }
}
