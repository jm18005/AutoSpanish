package com.autospanish.app.grammar

data class AdjectiveForms(val masculine: String, val feminine: String)

/**
 * Last-resort suffix-based feminine-form generator, used only when a word has no dictionary
 * entry at all (translated via ML Kit/MyMemory fallback). Words present in the dictionary get
 * their gender-agreement facts (including invariability) sourced directly instead of guessed here.
 */
object AdjectiveFormGenerator {

    fun resolve(masculineBase: String): AdjectiveForms {
        val lower = masculineBase.lowercase()
        val feminine = when {
            lower.endsWith("o") -> masculineBase.dropLast(1) + "a"
            lower.endsWith("án") -> masculineBase.dropLast(2) + "ana"
            lower.endsWith("ón") -> masculineBase.dropLast(2) + "ona"
            lower.endsWith("ín") -> masculineBase.dropLast(2) + "ina"
            lower.endsWith("or") -> masculineBase + "a"
            else -> masculineBase
        }
        return AdjectiveForms(masculineBase, feminine)
    }
}
