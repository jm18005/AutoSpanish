package com.autospanish.app.grammar

object ExampleSentenceGenerator {

    /** Only ever returns real, sourced example sentences — never a fabricated template. */
    fun generate(dictEntry: DictionaryEntry?): List<String> = dictEntry?.exampleSentences ?: emptyList()
}
