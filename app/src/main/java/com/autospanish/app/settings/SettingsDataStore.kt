package com.autospanish.app.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "autospanish_settings")

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val DARK_MODE_OVERRIDE = intPreferencesKey("dark_mode_override")
        val MODELS_DOWNLOADED = booleanPreferencesKey("mlkit_models_downloaded")
        val SPANISH_FIRST = booleanPreferencesKey("spanish_first")
    }

    val darkModeOverride: Flow<Int> = context.dataStore.data.map { it[Keys.DARK_MODE_OVERRIDE] ?: 0 }
    val modelsDownloaded: Flow<Boolean> = context.dataStore.data.map { it[Keys.MODELS_DOWNLOADED] ?: false }
    val spanishFirst: Flow<Boolean> = context.dataStore.data.map { it[Keys.SPANISH_FIRST] ?: true }

    suspend fun setDarkModeOverride(value: Int) {
        context.dataStore.edit { it[Keys.DARK_MODE_OVERRIDE] = value }
    }

    suspend fun setModelsDownloaded(value: Boolean) {
        context.dataStore.edit { it[Keys.MODELS_DOWNLOADED] = value }
    }

    suspend fun setSpanishFirst(value: Boolean) {
        context.dataStore.edit { it[Keys.SPANISH_FIRST] = value }
    }
}
