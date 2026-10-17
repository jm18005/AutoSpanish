package com.autospanish.app

import android.app.Application
import com.autospanish.app.data.AppDatabase
import com.autospanish.app.data.Repository
import com.autospanish.app.export.BackupManager
import com.autospanish.app.grammar.AssetDictionaries
import com.autospanish.app.settings.SettingsDataStore
import com.autospanish.app.translate.MlKitTranslateService
import com.autospanish.app.translate.WordGenerationPipeline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AutoSpanishApp : Application() {
    val appScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getInstance(this, appScope) }
    val dictionaries by lazy { AssetDictionaries.getInstance(this) }
    val mlKitTranslateService by lazy { MlKitTranslateService() }
    val pipeline by lazy { WordGenerationPipeline(dictionaries, mlKitTranslateService) }
    val repository by lazy { Repository(database.wordDao(), database.listDao(), pipeline) }
    val backupManager by lazy { BackupManager(database.wordDao(), database.listDao()) }
    val settingsDataStore by lazy { SettingsDataStore(this) }
}
