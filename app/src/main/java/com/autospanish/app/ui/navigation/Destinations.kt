package com.autospanish.app.ui.navigation

object Destinations {
    const val HOME = "home"
    const val LIST_DETAIL = "list/{listId}"
    const val ADD_WORD = "add_word/{targetListId}"
    const val OCR_CAPTURE = "ocr/{targetListId}"
    const val STUDY = "study/{source}"
    const val LEARN = "learn/{source}"
    const val SEARCH = "search"
    const val EXPORT = "export/{listId}"
    const val BACKUP = "backup"
    const val SETTINGS = "settings"

    fun listDetail(listId: Long) = "list/$listId"
    fun addWord(targetListId: Long) = "add_word/$targetListId"
    fun ocrCapture(targetListId: Long) = "ocr/$targetListId"
    fun study(source: String) = "study/$source"
    fun learn(source: String) = "learn/$source"
    fun export(listId: Long) = "export/$listId"
}
