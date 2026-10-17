package com.autospanish.app.export

import com.autospanish.app.data.WordEntity
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DocxExporter {

    fun export(title: String, words: List<WordEntity>, output: OutputStream) {
        ZipOutputStream(output).use { zip ->
            writeEntry(zip, "[Content_Types].xml", contentTypesXml())
            writeEntry(zip, "_rels/.rels", relsXml())
            writeEntry(zip, "word/document.xml", documentXml(title, words))
        }
    }

    private fun writeEntry(zip: ZipOutputStream, name: String, content: String) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun contentTypesXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>"""

    private fun relsXml() = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

    private fun documentXml(title: String, words: List<WordEntity>): String {
        val builder = StringBuilder()
        builder.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        builder.append("""<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body>""")
        builder.append(paragraph(title, bold = true))
        for (word in words) {
            val line = "${word.spanish.joinToString(", ")} — ${word.englishMeanings.joinToString("; ")}"
            builder.append(paragraph(line))
        }
        builder.append("</w:body></w:document>")
        return builder.toString()
    }

    private fun paragraph(text: String, bold: Boolean = false): String {
        val runProps = if (bold) "<w:rPr><w:b/></w:rPr>" else ""
        return "<w:p><w:r>$runProps<w:t xml:space=\"preserve\">${escapeXml(text)}</w:t></w:r></w:p>"
    }

    private fun escapeXml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
