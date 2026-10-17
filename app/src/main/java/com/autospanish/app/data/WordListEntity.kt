package com.autospanish.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

const val GLOBAL_LIST_ID = 1L

@Entity(tableName = "word_lists")
data class WordListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dateCreated: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
    val isGlobal: Boolean = false,
)
