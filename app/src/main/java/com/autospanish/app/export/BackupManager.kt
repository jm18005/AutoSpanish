package com.autospanish.app.export

import com.autospanish.app.data.GLOBAL_LIST_ID
import com.autospanish.app.data.ListDao
import com.autospanish.app.data.ListWordCrossRef
import com.autospanish.app.data.WordDao
import com.autospanish.app.data.WordEntity
import com.autospanish.app.data.WordListEntity
import com.google.gson.GsonBuilder
import java.io.InputStream
import java.io.OutputStream

data class BackupPayload(
    val words: List<WordEntity>,
    val lists: List<WordListEntity>,
    val listWordRefs: List<ListWordCrossRef>,
)

class BackupManager(
    private val wordDao: WordDao,
    private val listDao: ListDao,
) {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportBackup(output: OutputStream) {
        val payload = BackupPayload(
            words = wordDao.getAllWordsOnce(),
            lists = listDao.getAllListsOnce(),
            listWordRefs = wordDao.getAllListCrossRefsOnce(),
        )
        output.bufferedWriter().use { it.write(gson.toJson(payload)) }
    }

    suspend fun restoreReplacingAll(input: InputStream) {
        val payload = readPayload(input) ?: return
        wordDao.deleteAllListCrossRefs()
        wordDao.deleteAllWords()
        listDao.deleteAllNonGlobal()

        payload.words.forEach { wordDao.insert(it.copy(id = it.id)) }
        payload.lists.filterNot { it.isGlobal }.forEach { listDao.insert(it) }
        payload.listWordRefs.forEach { wordDao.insertListCrossRef(it) }
    }

    suspend fun restoreMerging(input: InputStream) {
        val payload = readPayload(input) ?: return

        val wordIdMap = mutableMapOf<Long, Long>()
        for (word in payload.words) {
            val existing = wordDao.findByAnySpanishForm(word.spanish)
            val newId = if (existing != null) {
                existing.id
            } else {
                wordDao.insert(word.copy(id = 0))
            }
            wordIdMap[word.id] = newId
        }

        val listIdMap = mutableMapOf<Long, Long>()
        listIdMap[GLOBAL_LIST_ID] = GLOBAL_LIST_ID
        val existingLists = listDao.getAllListsOnce()
        for (list in payload.lists) {
            if (list.isGlobal) continue
            val match = existingLists.find { it.name == list.name }
            val newId = match?.id ?: listDao.insert(list.copy(id = 0))
            listIdMap[list.id] = newId
        }

        payload.listWordRefs.forEach { ref ->
            val newListId = listIdMap[ref.listId] ?: return@forEach
            val newWordId = wordIdMap[ref.wordId] ?: return@forEach
            wordDao.insertListCrossRef(ListWordCrossRef(newListId, newWordId))
        }
    }

    private fun readPayload(input: InputStream): BackupPayload? = try {
        input.bufferedReader().use { gson.fromJson(it, BackupPayload::class.java) }
    } catch (e: Exception) {
        null
    }
}
