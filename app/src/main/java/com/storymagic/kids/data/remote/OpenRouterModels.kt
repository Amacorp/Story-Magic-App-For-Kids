package com.storymagic.kids.data.remote

import com.google.gson.annotations.SerializedName

data class OpenRouterRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<Message>,
    @SerializedName("temperature")
    val temperature: Double,
    @SerializedName("max_tokens")
    val maxTokens: Int = 1000
)

data class Message(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
)

data class OpenRouterResponse(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("choices")
    val choices: List<Choice> = emptyList(),
    @SerializedName("data")
    val data: List<ImageData>? = null
)

data class Choice(
    @SerializedName("message")
    val message: MessageResponse
)

data class MessageResponse(
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("content")
    val content: String? = null
)

data class ImageData(
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("b64_json")
    val b64Json: String? = null
)
