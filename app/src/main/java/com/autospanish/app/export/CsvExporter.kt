package com.autospanish.app.export

import com.autospanish.app.data.WordEntity
import java.io.OutputStream

object CsvExporter {
    fun export(words: List<WordEntity>, output: OutputStream) {
        output.bufferedWriter().use { writer ->
            writer.write("Spanish,English\n")
            words.forEach { word ->
                val spanish = escape(word.spanish.joinToString(", "))
                val english = escape(word.englishMeanings.joinToString("; "))
                writer.write("$spanish,$english\n")
            }
        }
    }

    private fun escape(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n")
        val escaped = value.replace("\"", "\"\"")
        return if (needsQuotes) "\"$escaped\"" else escaped
    }
}
