package com.autospanish.app.export

import com.autospanish.app.data.WordEntity
import java.io.OutputStream

object QuizletExporter {
    fun export(words: List<WordEntity>, output: OutputStream) {
        output.bufferedWriter().use { writer ->
            words.forEach { word ->
                val term = word.spanish.joinToString(", ").replace("\t", " ")
                val definition = word.englishMeanings.joinToString("; ").replace("\t", " ")
                writer.write("$term\t$definition\n")
            }
        }
    }
}
