package com.autospanish.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autospanish.app.data.AddWordResult
import com.autospanish.app.data.Repository
import com.autospanish.app.data.TranslationDirection
import com.autospanish.app.data.WordEntity
import com.autospanish.app.data.WordListEntity
import com.autospanish.app.export.BackupManager
import com.autospanish.app.settings.SettingsDataStore
import com.autospanish.app.translate.MlKitTranslateService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UndoAction {
    data class WordDeleted(val word: WordEntity) : UndoAction()
    data class ListDeleted(val list: WordListEntity, val wordIds: List<Long>) : UndoAction()
}

class AppViewModel(
    private val repository: Repository,
    private val backupManager: BackupManager,
    private val settingsDataStore: SettingsDataStore,
    private val mlKitTranslateService: MlKitTranslateService,
) : ViewModel() {

    val lists: StateFlow<List<WordListEntity>> = repository.allLists.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allWords: StateFlow<List<WordEntity>> = repository.allWords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val darkModeOverride: StateFlow<Int> = settingsDataStore.darkModeOverride.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val modelsDownloaded: StateFlow<Boolean> = settingsDataStore.modelsDownloaded.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val spanishFirst: StateFlow<Boolean> = settingsDataStore.spanishFirst.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _pendingUndo = MutableStateFlow<UndoAction?>(null)
    val pendingUndo: StateFlow<UndoAction?> = _pendingUndo

    private val _lastAddResult = MutableStateFlow<AddWordResult?>(null)
    val lastAddResult: StateFlow<AddWordResult?> = _lastAddResult

    fun wordsForList(listId: Long): Flow<List<WordEntity>> = repository.wordsForList(listId)
    fun searchWords(query: String): Flow<List<WordEntity>> = repository.search(query)
    fun weakestWords(limit: Int = 20): Flow<List<WordEntity>> = repository.weakestWords(limit)
    fun wordCountForList(listId: Long): Flow<Int> = repository.wordCountForList(listId)

    suspend fun resolveDirection(rawInput: String): TranslationDirection? = repository.resolveDirection(rawInput)

    fun addWord(rawInput: String, direction: TranslationDirection, allowOnline: Boolean, targetListId: Long?) {
        viewModelScope.launch {
            _lastAddResult.value = repository.addWordFromInput(rawInput, direction, allowOnline, targetListId)
        }
    }

    fun clearLastAddResult() {
        _lastAddResult.value = null
    }

    fun addWords(
        rawInputs: List<String>,
        direction: TranslationDirection,
        allowOnline: Boolean,
        targetListId: Long?,
        onComplete: (added: Int, duplicates: Int, failed: Int) -> Unit,
    ) {
        viewModelScope.launch {
            var added = 0
            var duplicates = 0
            var failed = 0
            for (input in rawInputs) {
                when (repository.addWordFromInput(input, direction, allowOnline, targetListId)) {
                    is AddWordResult.Added -> added++
                    is AddWordResult.AlreadyExists -> duplicates++
                    is AddWordResult.TranslationFailed -> failed++
                }
            }
            onComplete(added, duplicates, failed)
        }
    }

    fun linkWordToList(wordId: Long, listId: Long) = viewModelScope.launch { repository.linkWordToList(wordId, listId) }
    fun unlinkWordFromList(wordId: Long, listId: Long) = viewModelScope.launch { repository.unlinkWordFromList(wordId, listId) }

    fun updateWord(word: WordEntity) = viewModelScope.launch { repository.updateWord(word) }

    fun deleteWordGlobally(word: WordEntity) {
        viewModelScope.launch {
            repository.deleteWordGlobally(word)
            _pendingUndo.value = UndoAction.WordDeleted(word)
        }
    }

    fun createList(name: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch { onCreated(repository.createList(name)) }
    }

    fun renameList(list: WordListEntity, newName: String) = viewModelScope.launch { repository.renameList(list, newName) }

    fun deleteList(list: WordListEntity) {
        viewModelScope.launch {
            val wordIds = repository.getWordIdsForList(list.id)
            repository.deleteList(list)
            _pendingUndo.value = UndoAction.ListDeleted(list, wordIds)
        }
    }

    fun reorderLists(newOrder: List<WordListEntity>) = viewModelScope.launch { repository.reorderLists(newOrder) }

    fun mergeListIntoList(sourceListId: Long, targetListId: Long) =
        viewModelScope.launch { repository.mergeListIntoList(sourceListId, targetListId) }

    fun recordAnswer(word: WordEntity, correct: Boolean) = viewModelScope.launch { repository.recordAnswer(word, correct) }

    fun undo() {
        val action = _pendingUndo.value ?: return
        viewModelScope.launch {
            when (action) {
                is UndoAction.WordDeleted -> repository.restoreWord(action.word)
                is UndoAction.ListDeleted -> {
                    val newId = repository.createList(action.list.name)
                    action.wordIds.forEach { repository.linkWordToList(it, newId) }
                }
            }
            _pendingUndo.value = null
        }
    }

    fun clearUndo() {
        _pendingUndo.value = null
    }

    fun setDarkModeOverride(value: Int) = viewModelScope.launch { settingsDataStore.setDarkModeOverride(value) }
    fun setSpanishFirst(value: Boolean) = viewModelScope.launch { settingsDataStore.setSpanishFirst(value) }

    fun downloadOfflineModels(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = mlKitTranslateService.downloadModels()
            settingsDataStore.setModelsDownloaded(success)
            onResult(success)
        }
    }

    suspend fun exportBackup(output: java.io.OutputStream) = backupManager.exportBackup(output)
    suspend fun restoreBackup(input: java.io.InputStream, merge: Boolean) {
        if (merge) backupManager.restoreMerging(input) else backupManager.restoreReplacingAll(input)
    }

    class Factory(
        private val repository: Repository,
        private val backupManager: BackupManager,
        private val settingsDataStore: SettingsDataStore,
        private val mlKitTranslateService: MlKitTranslateService,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository, backupManager, settingsDataStore, mlKitTranslateService) as T
        }
    }
}
