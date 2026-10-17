package com.autospanish.app.translate

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class MlKitTranslateService {

    private val languageIdentifier = LanguageIdentification.getClient(
        LanguageIdentificationOptions.Builder().setConfidenceThreshold(0.5f).build()
    )

    private val esToEn = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.SPANISH)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
    )

    private val enToEs = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.SPANISH)
            .build()
    )

    suspend fun downloadModels(): Boolean {
        return try {
            val conditions = DownloadConditions.Builder().build()
            esToEn.downloadModelIfNeeded(conditions).await()
            enToEs.downloadModelIfNeeded(conditions).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun areModelsDownloaded(): Boolean {
        val manager = RemoteModelManager.getInstance()
        val models = manager.getDownloadedModels(com.google.mlkit.nl.translate.TranslateRemoteModel::class.java).await()
        val languages = models.map { it.language }.toSet()
        return TranslateLanguage.SPANISH in languages && TranslateLanguage.ENGLISH in languages
    }

    suspend fun translateEsToEn(text: String): String? = try {
        esToEn.translate(text).await()
    } catch (e: Exception) {
        null
    }

    suspend fun translateEnToEs(text: String): String? = try {
        enToEs.translate(text).await()
    } catch (e: Exception) {
        null
    }

    /** Returns an ISO 639-1 code ("en", "es", ...) for confidently-identified text, or null if undetermined. */
    suspend fun identifyLanguage(text: String): String? = try {
        languageIdentifier.identifyLanguage(text).await().takeUnless { it == "und" }
    } catch (e: Exception) {
        null
    }

    fun close() {
        esToEn.close()
        enToEs.close()
        languageIdentifier.close()
    }
}
