package com.autospanish.app.translate

import com.autospanish.app.grammar.COMMON_SPANISH_WORDS
import com.autospanish.app.grammar.DictionaryEntry
import com.autospanish.app.grammar.glossVariants

data class SenseResolution(val entry: DictionaryEntry, val extraSynonyms: List<String>)

/**
 * Picks which Spanish dictionary entry best matches an English query when more than one entry
 * glosses that word. Entries transitively linked by an explicit synonym reference (either
 * direction, chained through any number of intermediate entries) are the same sense and collapse
 * to the best one. Entries that remain unlinked are genuinely different meanings (e.g. "orange"
 * the fruit vs. "orange" the color) — rather than asking the user to pick (they have no way to
 * know), the most common Spanish word wins, like a machine translator would, and the other
 * senses' words are kept as synonyms on the resolved entry.
 */
object SenseResolver {
    fun resolve(raw: String, candidates: List<DictionaryEntry>): SenseResolution {
        val rawLower = raw.lowercase()
        val exact = candidates.filter { entry -> entry.english.any { rawLower in glossVariants(it) } }
        val pool = exact.ifEmpty { candidates }

        val clusterReps = clusterBySynonym(pool).map { bestOf(it, rawLower) }
        val chosen = bestOf(clusterReps, rawLower)
        val extraSynonyms = clusterReps.filterNot { it === chosen }.map { it.spanish }
        return SenseResolution(chosen, extraSynonyms)
    }

    /** Groups entries transitively connected by a synonym reference using union-find, so a chain
     * like A-points-to-B and B-points-to-C merges A, B, and C into one cluster regardless of scan order. */
    private fun clusterBySynonym(pool: List<DictionaryEntry>): List<List<DictionaryEntry>> {
        val parent = IntArray(pool.size) { it }
        fun find(x: Int): Int {
            var root = x
            while (parent[root] != root) root = parent[root]
            parent[x] = root
            return root
        }
        fun union(a: Int, b: Int) {
            val ra = find(a)
            val rb = find(b)
            if (ra != rb) parent[ra] = rb
        }
        for (i in pool.indices) {
            for (j in i + 1 until pool.size) {
                if (isSynonymOf(pool[i], pool[j]) || isSynonymOf(pool[j], pool[i])) union(i, j)
            }
        }
        return pool.indices.groupBy { find(it) }.values.map { indices -> indices.map { pool[it] } }
    }

    private fun isSynonymOf(entry: DictionaryEntry, other: DictionaryEntry) =
        entry.synonyms?.any { it.equals(other.spanish, ignoreCase = true) } == true

    private fun bestOf(cluster: List<DictionaryEntry>, rawLower: String): DictionaryEntry = cluster.sortedWith(
        // Real-world frequency is the strongest signal of "most common word" — stronger than any
        // heuristic derivable from the scraped data itself — so a known-common Spanish word wins
        // over a rarer synonym even if the rarer one happens to list its gloss first or has more
        // metadata recorded. Beneath that, a word's first-listed gloss matching is the next best
        // signal that the query is its primary sense (scraped gloss order isn't fully reliable, so
        // "matches some gloss" is kept as a weaker fallback tiebreak beneath it).
        compareByDescending<DictionaryEntry> { it.spanish.lowercase() in COMMON_SPANISH_WORDS }
            .thenByDescending { candidate ->
                candidate.english.firstOrNull()?.let { rawLower in glossVariants(it) } == true
            }
            .thenByDescending { candidate -> candidate.english.any { rawLower in glossVariants(it) } }
            .thenByDescending { candidate -> cluster.any { it !== candidate && isSynonymOf(it, candidate) } }
            .thenBy { candidate -> cluster.any { it !== candidate && isSynonymOf(candidate, it) } }
            .thenByDescending { !it.exampleSentences.isNullOrEmpty() }
            .thenByDescending { !it.synonyms.isNullOrEmpty() }
    ).first()
}
