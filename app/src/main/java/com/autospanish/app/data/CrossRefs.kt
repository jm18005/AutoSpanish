package com.autospanish.app.data

import androidx.room.Entity

@Entity(tableName = "list_word_cross_ref", primaryKeys = ["listId", "wordId"])
data class ListWordCrossRef(val listId: Long, val wordId: Long)
