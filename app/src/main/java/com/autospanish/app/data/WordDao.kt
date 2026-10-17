package com.autospanish.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: WordEntity): Long

    @Update
    suspend fun update(word: WordEntity)

    @Delete
    suspend fun delete(word: WordEntity)

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getById(id: Long): WordEntity?

    /** Matches if any of [forms] equals any of the stored Spanish forms — spanish is now a
     * comma-editable list, so exact-string SQL matching against its JSON encoding won't do. */
    suspend fun findByAnySpanishForm(forms: List<String>): WordEntity? =
        getAllWordsOnce().firstOrNull { entity -> entity.spanish.any { s -> forms.any { it.equals(s, ignoreCase = true) } } }

    @Query("SELECT * FROM words ORDER BY dateAdded DESC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query(
        """
        SELECT words.* FROM words
        INNER JOIN list_word_cross_ref ON words.id = list_word_cross_ref.wordId
        WHERE list_word_cross_ref.listId = :listId
        ORDER BY words.dateAdded DESC
        """
    )
    fun getWordsForList(listId: Long): Flow<List<WordEntity>>

    @Query(
        """
        SELECT * FROM words
        WHERE LOWER(spanish) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(englishMeanings) LIKE '%' || LOWER(:query) || '%'
        ORDER BY dateAdded DESC
        """
    )
    fun search(query: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE timesTried > 0 ORDER BY (CAST(timesCorrect AS FLOAT) / timesTried) ASC LIMIT :limit")
    fun getWeakestWords(limit: Int): Flow<List<WordEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertListCrossRef(ref: ListWordCrossRef)

    @Query("DELETE FROM list_word_cross_ref WHERE listId = :listId AND wordId = :wordId")
    suspend fun removeFromList(listId: Long, wordId: Long)

    @Query("SELECT wordId FROM list_word_cross_ref WHERE listId = :listId")
    suspend fun getWordIdsForListOnce(listId: Long): List<Long>

    @Query("SELECT COUNT(*) FROM list_word_cross_ref WHERE listId = :listId")
    fun countForList(listId: Long): Flow<Int>

    @Query("SELECT * FROM words")
    suspend fun getAllWordsOnce(): List<WordEntity>

    @Query("SELECT * FROM list_word_cross_ref")
    suspend fun getAllListCrossRefsOnce(): List<ListWordCrossRef>

    @Query("DELETE FROM words")
    suspend fun deleteAllWords()

    @Query("DELETE FROM list_word_cross_ref")
    suspend fun deleteAllListCrossRefs()
}
