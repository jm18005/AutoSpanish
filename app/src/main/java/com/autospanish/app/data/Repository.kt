package com.autospanish.app.data

import com.autospanish.app.translate.GeneratedWord
import com.autospanish.app.translate.TranslationOutcome
import com.autospanish.app.translate.WordGenerationPipeline
import kotlinx.coroutines.flow.Flow

sealed class AddWordResult {
    data class Added(val word: WordEntity) : AddWordResult()
    data class AlreadyExists(val word: WordEntity) : AddWordResult()
    object TranslationFailed : AddWordResult()
}

class Repository(
    private val wordDao: WordDao,
    private val listDao: ListDao,
    private val pipeline: WordGenerationPipeline,
) {
    val allWords: Flow<List<WordEntity>> = wordDao.getAllWords()
    val allLists: Flow<List<WordListEntity>> = listDao.getAllLists()

    fun wordsForList(listId: Long): Flow<List<WordEntity>> = wordDao.getWordsForList(listId)
    fun search(query: String): Flow<List<WordEntity>> = wordDao.search(query)
    fun weakestWords(limit: Int = 20): Flow<List<WordEntity>> = wordDao.getWeakestWords(limit)
    fun wordCountForList(listId: Long): Flow<Int> = wordDao.countForList(listId)

    suspend fun resolveDirection(rawInput: String): TranslationDirection? = pipeline.resolveDirectionOrNull(rawInput)

    suspend fun addWordFromInput(
        rawInput: String,
        direction: TranslationDirection,
        allowOnline: Boolean,
        targetListId: Long?,
    ): AddWordResult {
        val generated: GeneratedWord = when (val outcome = pipeline.generate(rawInput, direction, allowOnline)) {
            is TranslationOutcome.Success -> outcome.word
            TranslationOutcome.Failed -> return AddWordResult.TranslationFailed
        }
        return finalizeAdd(generated, targetListId)
    }

    private suspend fun finalizeAdd(generated: GeneratedWord, targetListId: Long?): AddWordResult {
        val existing = wordDao.findByAnySpanishForm(listOf(generated.spanish))
        if (existing != null) {
            targetListId?.let { linkWordToList(existing.id, it) }
            linkWordToList(existing.id, GLOBAL_LIST_ID)
            return AddWordResult.AlreadyExists(existing)
        }

        val entity = WordEntity(
            spanish = listOf(generated.spanish),
            englishMeanings = generated.englishMeanings,
            partOfSpeech = generated.partOfSpeech,
            gender = generated.gender,
            article = generated.article,
            plural = generated.plural,
            isReflexive = generated.isReflexive,
            counterpartForm = generated.counterpartForm,
            irregularityType = generated.irregularityType,
            conjugations = generated.conjugations,
            serEstarHint = generated.serEstarHint,
            falseFriendWarning = generated.falseFriendWarning,
            synonyms = generated.synonyms,
            regionalNote = generated.regionalNote,
            exampleSentences = generated.exampleSentences,
            prepositionPairing = generated.prepositionPairing,
        )
        val id = wordDao.insert(entity)
        linkWordToList(id, GLOBAL_LIST_ID)
        targetListId?.let { if (it != GLOBAL_LIST_ID) linkWordToList(id, it) }
        return AddWordResult.Added(entity.copy(id = id))
    }

    suspend fun linkWordToList(wordId: Long, listId: Long) = wordDao.insertListCrossRef(ListWordCrossRef(listId, wordId))
    suspend fun unlinkWordFromList(wordId: Long, listId: Long) = wordDao.removeFromList(listId, wordId)

    suspend fun deleteWordGlobally(word: WordEntity) = wordDao.delete(word)
    suspend fun updateWord(word: WordEntity) = wordDao.update(word)
    suspend fun restoreWord(word: WordEntity) {
        val id = wordDao.insert(word)
        linkWordToList(id, GLOBAL_LIST_ID)
    }

    suspend fun createList(name: String): Long = listDao.insert(WordListEntity(name = name))
    suspend fun renameList(list: WordListEntity, newName: String) {
        if (list.isGlobal) return
        listDao.update(list.copy(name = newName))
    }
    suspend fun deleteList(list: WordListEntity) {
        if (list.isGlobal) return
        listDao.delete(list)
    }
    suspend fun reorderLists(lists: List<WordListEntity>) {
        lists.forEachIndexed { index, list -> listDao.update(list.copy(sortOrder = index)) }
    }

    suspend fun mergeListIntoList(sourceListId: Long, targetListId: Long) {
        wordDao.getWordIdsForListOnce(sourceListId).forEach { linkWordToList(it, targetListId) }
    }

    suspend fun recordAnswer(word: WordEntity, correct: Boolean) {
        wordDao.update(
            word.copy(
                timesTried = word.timesTried + 1,
                timesCorrect = word.timesCorrect + if (correct) 1 else 0,
            )
        )
    }

    suspend fun getWordIdsForList(listId: Long): List<Long> = wordDao.getWordIdsForListOnce(listId)
}
