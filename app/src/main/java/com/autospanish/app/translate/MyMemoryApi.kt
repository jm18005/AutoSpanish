package com.autospanish.app.translate

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class MyMemoryResponseData(val translatedText: String?)
data class MyMemoryMatch(val translation: String?, val quality: String? = null)
data class MyMemoryResponse(val responseData: MyMemoryResponseData?, val matches: List<MyMemoryMatch>?)

interface MyMemoryApi {
    @GET("get")
    suspend fun translate(@Query("q") text: String, @Query("langpair") langPair: String): MyMemoryResponse
}

object MyMemoryClient {
    val api: MyMemoryApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.mymemory.translated.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MyMemoryApi::class.java)
    }

    suspend fun translateAndCollectMeanings(text: String, langPair: String): List<String> {
        return try {
            val response = api.translate(text, langPair)
            val meanings = mutableSetOf<String>()
            response.responseData?.translatedText?.let { meanings.add(it) }
            // Quality "0" means MyMemory has no real match for the query and is just padding the
            // response — don't treat that as a found translation.
            response.matches?.forEach { if (it.quality != "0") it.translation?.let { t -> meanings.add(t) } }
            meanings.map { it.trim() }.filter { it.isNotEmpty() }.take(5)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
