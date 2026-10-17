package com.autospanish.app.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

object OcrTextExtractor {

    suspend fun extractWords(bitmap: Bitmap): List<String> {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = try {
            recognizer.process(image).await()
        } catch (e: Exception) {
            null
        }
        val words = mutableListOf<String>()
        result?.textBlocks?.forEach { block ->
            block.lines.forEach { line ->
                line.elements.forEach { element ->
                    val cleaned = element.text.trim().trim { c -> !c.isLetter() }
                    if (cleaned.length > 1) words.add(cleaned)
                }
            }
        }
        return words.distinctBy { it.lowercase() }
    }
}
