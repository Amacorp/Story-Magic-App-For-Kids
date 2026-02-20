package com.storymagic.kids.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenRouterApi {

    @POST("chat/completions")
    suspend fun generateCompletion(
        @Header("Authorization") auth: String,
        @Header("HTTP-Referer") referer: String,
        @Header("X-Title") title: String,
        @Body request: OpenRouterRequest
    ): Response<OpenRouterResponse>
    
    @POST("chat/completions")
    suspend fun generateImage(
        @Header("Authorization") auth: String,
        @Header("HTTP-Referer") referer: String,
        @Header("X-Title") title: String,
        @Body request: OpenRouterRequest
    ): Response<OpenRouterResponse>
    
    @POST("chat/completions")
    suspend fun generateImageRaw(
        @Header("Authorization") auth: String,
        @Header("HTTP-Referer") referer: String,
        @Header("X-Title") title: String,
        @Body request: OpenRouterRequest
    ): Response<ResponseBody>
}
