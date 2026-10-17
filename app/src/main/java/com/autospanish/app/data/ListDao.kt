package com.autospanish.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: WordListEntity): Long

    @Update
    suspend fun update(list: WordListEntity)

    @Delete
    suspend fun delete(list: WordListEntity)

    @Query("SELECT * FROM word_lists ORDER BY isGlobal DESC, sortOrder ASC, dateCreated DESC")
    fun getAllLists(): Flow<List<WordListEntity>>

    @Query("SELECT * FROM word_lists WHERE id = :id")
    suspend fun getById(id: Long): WordListEntity?

    @Query("SELECT COUNT(*) FROM word_lists")
    suspend fun count(): Int

    @Query("SELECT * FROM word_lists")
    suspend fun getAllListsOnce(): List<WordListEntity>

    @Query("DELETE FROM word_lists WHERE isGlobal = 0")
    suspend fun deleteAllNonGlobal()
}
