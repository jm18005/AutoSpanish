package com.autospanish.app.translate

import com.autospanish.app.grammar.DictionaryEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SenseResolverTest {

    private fun entry(
        spanish: String,
        english: List<String>,
        pos: String = "NOUN",
        synonyms: List<String>? = null,
        exampleSentences: List<String>? = null,
    ) = DictionaryEntry(spanish = spanish, english = english, pos = pos, synonyms = synonyms, exampleSentences = exampleSentences)

    @Test
    fun `single candidate resolves directly`() {
        val casa = entry("casa", listOf("house"))
        val result = SenseResolver.resolve("house", listOf(casa))
        assertEquals("casa", result.entry.spanish)
        assertTrue(result.extraSynonyms.isEmpty())
    }

    @Test
    fun `synonym-linked candidates collapse to the pointed-to word`() {
        val catedratico = entry("catedrático", listOf("professor"), synonyms = listOf("profesor"))
        val profesor = entry("profesor", listOf("professor"))
        val result = SenseResolver.resolve("professor", listOf(catedratico, profesor))
        assertEquals("profesor", result.entry.spanish)
        assertTrue(result.extraSynonyms.isEmpty())
    }

    @Test
    fun `unlinked noun and adjective senses of the same gloss resolve to one, keeping the other as a synonym`() {
        val naranja = entry("naranja", listOf("orange"), pos = "NOUN")
        val anaranjado = entry("anaranjado", listOf("orange"), pos = "ADJECTIVE", exampleSentences = listOf("ex"))
        val result = SenseResolver.resolve("orange", listOf(naranja, anaranjado))
        val allSpanish = setOf(result.entry.spanish) + result.extraSynonyms
        assertEquals(setOf("naranja", "anaranjado"), allSpanish)
        assertEquals(1, result.extraSynonyms.size)
    }

    @Test
    fun `verb candidates prefer the entry whose primary gloss matches exactly`() {
        val correr = entry("correr", listOf("run"), pos = "VERB")
        val correrPrisa = entry("corre prisa", listOf("there's a need to run"), pos = "VERB")
        val result = SenseResolver.resolve("run", listOf(correrPrisa, correr))
        assertEquals("correr", result.entry.spanish)
    }

    @Test
    fun `adjective candidates prefer the entry with an example sentence when otherwise tied`() {
        // Neither spelling is a known-common word, so this exercises the tiebreak below that
        // signal. Linked by a synonym reference so they cluster as one sense.
        val enorme = entry("enorme", listOf("huge"), pos = "ADJECTIVE", synonyms = listOf("colosal"))
        val colosal = entry("colosal", listOf("huge"), pos = "ADJECTIVE", exampleSentences = listOf("Es un edificio colosal. — It's a huge building."))
        val result = SenseResolver.resolve("huge", listOf(enorme, colosal))
        assertEquals("colosal", result.entry.spanish)
    }

    @Test
    fun `unlinked preposition candidates sharing a gloss resolve to one, keeping the other as a synonym`() {
        val para = entry("para", listOf("for"), pos = "OTHER")
        val por = entry("por", listOf("for"), pos = "OTHER")
        val result = SenseResolver.resolve("for", listOf(para, por))
        val allSpanish = setOf(result.entry.spanish) + result.extraSynonyms
        assertEquals(setOf("para", "por"), allSpanish)
        assertEquals(1, result.extraSynonyms.size)
    }

    @Test
    fun `no exact gloss match still resolves using the full candidate pool`() {
        val mai = entry("mai", listOf("mother's affectionate form", "riverbed"), pos = "NOUN")
        val result = SenseResolver.resolve("mother", listOf(mai))
        assertEquals("mai", result.entry.spanish)
    }
}
