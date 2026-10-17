package com.autospanish.app.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String = gson.toJson(value ?: emptyList<String>())

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromConjugations(value: Map<String, List<String>>?): String = gson.toJson(value ?: emptyMap<String, List<String>>())

    @TypeConverter
    fun toConjugations(value: String): Map<String, List<String>> {
        val type = object : TypeToken<Map<String, List<String>>>() {}.type
        return gson.fromJson(value, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromPartOfSpeech(value: PartOfSpeech): String = value.name

    @TypeConverter
    fun toPartOfSpeech(value: String): PartOfSpeech = PartOfSpeech.valueOf(value)

    @TypeConverter
    fun fromGender(value: Gender): String = value.name

    @TypeConverter
    fun toGender(value: String): Gender = Gender.valueOf(value)
}
