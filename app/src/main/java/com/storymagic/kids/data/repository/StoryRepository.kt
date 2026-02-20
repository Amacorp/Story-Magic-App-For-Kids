package com.storymagic.kids.data.repository

import com.storymagic.kids.data.local.StoryDao
import com.storymagic.kids.data.local.StoryEntity
import com.storymagic.kids.data.remote.Message
import com.storymagic.kids.data.remote.OpenRouterApi
import com.storymagic.kids.data.remote.OpenRouterRequest
import com.storymagic.kids.domain.LogManager
import com.storymagic.kids.domain.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class StoryRepository(
    private val openRouterApi: OpenRouterApi,
    private val storyDao: StoryDao,
    private val preferencesRepository: PreferencesRepository
) {
    
    companion object {
        private const val REFERER = "https://storymagic.app"
        private const val TITLE = "StoryMagic Kids"
        private const val MAX_TOKENS = 1500
    }
    
    fun generateStory(
        childName: String,
        age: Int,
        gender: String,
        genres: List<String>,
        belovedObject: String,
        petType: String?,
        petName: String?,
        moral: String
    ): Flow<Resource<StoryEntity>> = flow {
        emit(Resource.Loading)
        LogManager.log("StoryRepository", "INFO", "========== STORY GENERATION STARTED ==========")
        LogManager.log("StoryRepository", "INFO", "Child: $childName, Age: $age, Gender: $gender")
        LogManager.log("StoryRepository", "INFO", "Genres: ${genres.joinToString(", ")}")
        LogManager.log("StoryRepository", "INFO", "Beloved Object: $belovedObject")
        LogManager.log("StoryRepository", "INFO", "Pet: ${petType ?: "None"} (${petName ?: "N/A"})")
        LogManager.log("StoryRepository", "INFO", "Moral: $moral")

        val selectedModel = preferencesRepository.getStoryModel()
        val temperature = preferencesRepository.getCreativity()
        val apiKey = preferencesRepository.getApiKey()

        LogManager.log("StoryRepository", "INFO", "Model: $selectedModel")
        LogManager.log("StoryRepository", "INFO", "Temperature: $temperature")

        if (apiKey.isBlank()) {
            LogManager.log("StoryRepository", "ERROR", "API key is empty")
            emit(Resource.Error("Please set your OpenRouter API key in Settings!"))
            return@flow
        }
        
        if (apiKey.length < 20) {
            LogManager.log("StoryRepository", "ERROR", "API key seems invalid")
            emit(Resource.Error("API key appears invalid. Please check it!"))
            return@flow
        }

        val prompt = buildPrompt(
            childName = childName,
            age = age,
            gender = gender,
            genres = genres,
            belovedObject = belovedObject,
            petType = petType,
            petName = petName,
            moral = moral
        )

        LogManager.log("StoryRepository", "DEBUG", "Prompt length: ${prompt.length} characters")

        val request = OpenRouterRequest(
            model = selectedModel,
            messages = listOf(Message("user", prompt)),
            temperature = temperature,
            maxTokens = MAX_TOKENS
        )

        val startTime = System.currentTimeMillis()

        try {
            LogManager.log("StoryRepository", "INFO", "Making API request to OpenRouter...")

            val response = withContext(Dispatchers.IO) {
                openRouterApi.generateCompletion(
                    auth = "Bearer $apiKey",
                    referer = REFERER,
                    title = TITLE,
                    request = request
                )
            }

            val responseTime = System.currentTimeMillis() - startTime
            LogManager.log("StoryRepository", "INFO", "API response received in ${responseTime}ms")
            LogManager.log("StoryRepository", "INFO", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val responseBody = response.body()
                
                if (responseBody != null && responseBody.choices.isNotEmpty()) {
                    val content = responseBody.choices[0].message.content
                    LogManager.log("StoryRepository", "INFO", "Response content length: ${content?.length ?: 0}")
                    
                    val story = parseStoryResponse(
                        content = content ?: "",
                        childName = childName,
                        age = age,
                        gender = gender,
                        genres = genres,
                        belovedObject = belovedObject,
                        petType = petType,
                        petName = petName,
                        moral = moral
                    )
                    
                    val storyId = storyDao.insertStory(story)
                    LogManager.log("StoryRepository", "INFO", "========== STORY SAVED ==========")
                    LogManager.log("StoryRepository", "INFO", "Story ID: $storyId, Title: ${story.title}")

                    emit(Resource.Success(story.copy(id = storyId.toInt())))
                } else {
                    LogManager.log("StoryRepository", "ERROR", "Empty response")
                    emit(Resource.Error("The magic didn't work. Please try again!"))
                }
            } else {
                val errorCode = response.code()
                val errorBody = response.errorBody()?.string()
                LogManager.log("StoryRepository", "ERROR", "API error $errorCode: $errorBody")
                emit(Resource.Error(getUserFriendlyError(errorCode)))
            }
        } catch (e: IOException) {
            LogManager.log("StoryRepository", "ERROR", "Network error: ${e.message}")
            emit(Resource.Error("Network error! Check your internet connection."))
        } catch (e: Exception) {
            LogManager.log("StoryRepository", "ERROR", "Error: ${e.message}")
            emit(Resource.Error("Something went wrong. Please try again!"))
        }
    }.flowOn(Dispatchers.IO)
    
    private fun buildPrompt(
        childName: String,
        age: Int,
        gender: String,
        genres: List<String>,
        belovedObject: String,
        petType: String?,
        petName: String?,
        moral: String
    ): String {
        val genreString = genres.joinToString(", ")
        val petLine = if (petType != null && petName != null) {
            "- Include their $petType named $petName as a companion character"
        } else {
            ""
        }
        val pronoun = if (gender == "Girl") "she" else "he"
        val pronounCapitalized = if (gender == "Girl") "She" else "He"
        
        return """
Create a wonderful bedtime story (400-500 words) for a ${age}-year-old ${gender.lowercase()} named $childName.

IMPORTANT: Use "$pronoun"/"$pronounCapitalized" pronouns throughout the story!

STORY REQUIREMENTS:
- Genre: $genreString
- Must include their beloved object: $belovedObject
$petLine
- Theme/Moral: $moral
- Tone: Gentle, imaginative, warm, and encouraging
- Language: Simple vocabulary appropriate for a $age-year-old child
- Make $childName the hero of the story

STRUCTURE:
1. Beginning: Set a magical scene and introduce $childName
2. Middle: A gentle adventure or challenge $pronoun must face
3. End: A happy resolution that teaches the moral of $moral

IMPORTANT: Write a complete, engaging story that a child would love to hear at bedtime!

FORMAT YOUR RESPONSE EXACTLY LIKE THIS:
TITLE: [A creative, magical title for the story]

DESCRIPTION: [2-3 sentence exciting summary of what happens]

STORY:
[The complete story text with clear paragraph breaks. Make it engaging and magical!]
        """.trimIndent()
    }
    
    private fun parseStoryResponse(
        content: String,
        childName: String,
        age: Int,
        gender: String,
        genres: List<String>,
        belovedObject: String,
        petType: String?,
        petName: String?,
        moral: String
    ): StoryEntity {
        LogManager.log("StoryRepository", "DEBUG", "Parsing story response")
        
        var title = "A Magical Adventure"
        var description = "A wonderful story just for you!"
        var storyText = content
        
        val titleMatch = Regex("TITLE:\\s*(.+)").find(content)
        titleMatch?.let { title = it.groupValues[1].trim() }
        
        val descMatch = Regex("DESCRIPTION:\\s*(.+)").find(content)
        descMatch?.let { description = it.groupValues[1].trim() }
        
        val storyMatch = Regex("STORY:\\s*([\\s\\S]+)").find(content)
        storyMatch?.let { storyText = it.groupValues[1].trim() }
        
        if (storyText == content && content.length > 200) {
            val lines = content.split("\n\n")
            if (lines.size >= 3) {
                title = lines.firstOrNull()?.trim() ?: "A Magical Adventure"
                description = lines.getOrNull(1)?.trim() ?: "A wonderful story!"
                storyText = lines.drop(2).joinToString("\n\n").trim()
            }
        }
        
        val wordCount = storyText.split("\\s+".toRegex()).size
        val readingTimeMinutes = maxOf(1, (wordCount / 150.0).toInt())
        
        val gradientColors = getRandomGradientColors()
        
        return StoryEntity(
            childName = childName,
            childAge = age,
            childGender = gender,
            genres = genres.joinToString(","),
            belovedObject = belovedObject,
            petType = petType,
            petName = petName,
            moral = moral,
            title = title,
            description = description,
            storyText = storyText,
            imageUrl = null,
            imageDescription = "Story thumbnail",
            gradientColorStart = gradientColors.first,
            gradientColorEnd = gradientColors.second,
            isFavorite = false,
            readingTimeMinutes = readingTimeMinutes
        )
    }
    
    private fun getRandomGradientColors(): Pair<String, String> {
        val gradients = listOf(
            Pair("#FF6B9D", "#00D4FF"),
            Pair("#FFDD00", "#FF6B9D"),
            Pair("#00D4FF", "#2ED573"),
            Pair("#9B59B6", "#FF6B9D"),
            Pair("#FF9F43", "#FFDD00"),
            Pair("#00CEC9", "#00D4FF"),
            Pair("#2ED573", "#FFDD00"),
            Pair("#FF6B9D", "#9B59B6"),
            Pair("#A55EEA", "#00D4FF"),
            Pair("#FF9F43", "#00CEC9"),
        )
        return gradients.random()
    }
    
    private fun getUserFriendlyError(code: Int): String {
        return when (code) {
            401 -> "API key is incorrect. Please check Settings!"
            403 -> "Access denied. Verify your API key!"
            429 -> "Too many requests! Wait a moment."
            500, 502, 503 -> "Server is busy. Try again soon!"
            else -> "Something went wrong. Try again!"
        }
    }
    
    fun getAllStories() = storyDao.getAllStories()
    
    fun getFavoriteStories() = storyDao.getFavoriteStories()
    
    suspend fun getStoryById(id: Int) = storyDao.getStoryById(id)
    
    fun searchStories(query: String) = storyDao.searchStories(query)
    
    suspend fun updateStory(story: StoryEntity) {
        storyDao.updateStory(story)
        LogManager.log("StoryRepository", "INFO", "Story updated: ID ${story.id}")
    }
    
    suspend fun deleteStory(story: StoryEntity) {
        storyDao.deleteStory(story)
        LogManager.log("StoryRepository", "INFO", "Story deleted: ID ${story.id}")
    }
    
    suspend fun getStoryCountToday(startOfDay: Long): Int {
        return storyDao.getStoryCountToday(startOfDay)
    }
}
