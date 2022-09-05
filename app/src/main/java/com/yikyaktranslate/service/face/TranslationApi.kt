package com.yikyaktranslate.service.face

import com.yikyaktranslate.model.Language
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TranslationApi {

    @GET("/languages")
    suspend fun getLanguages(): Response<List<Language>>

    @POST("/translate")
    suspend fun translateString(@Body request: MultipartBody): Response<Translation>
}