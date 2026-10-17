package com.autospanish.app.translate

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Confirms genuinely ambiguous English words (multiple unrelated Spanish senses) auto-resolve to
 * one entry rather than surfacing a picker — the user has no way to know which sense is meant, so
 * [SenseResolver] picks the most common word and keeps the other sense(s) as synonyms instead.
 */
class AmbiguousWordTest {

    @Test
    fun `orange resolves to one sense and keeps the other as a synonym`() {
        val matches = DictionaryTestFixture.byEnglish["orange"]
        assertTrue("expected dictionary entries for \"orange\"", !matches.isNullOrEmpty())
        val resolution = SenseResolver.resolve("orange", matches!!)
        assertTrue("expected the other sense to survive as a synonym, got $resolution", resolution.extraSynonyms.isNotEmpty())
    }
}
