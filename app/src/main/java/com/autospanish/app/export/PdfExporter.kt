package com.autospanish.app.export

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.autospanish.app.data.WordEntity
import java.io.OutputStream

object PdfExporter {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val LINE_HEIGHT = 24f

    fun export(title: String, words: List<WordEntity>, output: OutputStream) {
        val document = PdfDocument()
        val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 13f }

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas
        var y = MARGIN + 20f
        canvas.drawText(title, MARGIN, y, titlePaint)
        y += LINE_HEIGHT * 1.5f

        for (word in words) {
            if (y > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = page.canvas
                y = MARGIN
            }
            val line = "${word.spanish.joinToString(", ")}  —  ${word.englishMeanings.joinToString("; ")}"
            canvas.drawText(line, MARGIN, y, bodyPaint)
            y += LINE_HEIGHT
        }
        document.finishPage(page)
        document.writeTo(output)
        document.close()
    }
}
