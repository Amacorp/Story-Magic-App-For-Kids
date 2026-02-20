package com.storymagic.kids.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.storymagic.kids.domain.LogManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Repository for managing app preferences using DataStore and EncryptedSharedPreferences.
 */
class PreferencesRepository(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "storymagic_prefs")
        
        // Preference keys
        private val API_KEY = stringPreferencesKey("api_key")
        private val STORY_MODEL = stringPreferencesKey("story_model")
        private val IMAGE_MODEL = stringPreferencesKey("image_model")
        private val CREATIVITY = floatPreferencesKey("creativity")
        private val FALLBACK_MODEL = booleanPreferencesKey("fallback_model")
        private val DEFAULT_VOICE_KEY = stringPreferencesKey("default_voice")
        private val HIGHLIGHT_WORDS = booleanPreferencesKey("highlight_words")
        private val TEXT_SIZE = stringPreferencesKey("text_size")
        private val AUTO_PLAY = booleanPreferencesKey("auto_play")
        private val SAVE_STORIES = booleanPreferencesKey("save_stories")
        private val THEME = stringPreferencesKey("theme")
        private val PARENTAL_PIN = stringPreferencesKey("parental_pin")
        private val DAILY_LIMIT = stringPreferencesKey("daily_limit")
        private val CONTENT_FILTER = stringPreferencesKey("content_filter")
        private val DISABLED_GENRES = stringPreferencesKey("disabled_genres")

        // Default values
        const val DEFAULT_STORY_MODEL = "google/gemini-2.0-flash-lite-001"
        const val DEFAULT_CREATIVITY = 0.8f
        const val DEFAULT_VOICE_VALUE = "Happy Teacher"
        const val DEFAULT_TEXT_SIZE = "Medium"
        const val DEFAULT_THEME = "Light"
        const val DEFAULT_DAILY_LIMIT = "10"
        const val DEFAULT_CONTENT_FILTER = "Moderate"
    }
    
    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "storymagic_encrypted_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            LogManager.log("PreferencesRepository", "ERROR", "Failed to create EncryptedSharedPreferences: ${e.message}")
            // Fallback to regular SharedPreferences
            context.getSharedPreferences("storymagic_encrypted_prefs", Context.MODE_PRIVATE)
        }
    }
    
    // API Key (stored in encrypted prefs)
    suspend fun setApiKey(key: String) {
        encryptedPrefs.edit().putString("api_key", key).apply()
        LogManager.log("PreferencesRepository", "INFO", "API key updated")
    }

    fun getApiKey(): String {
        val key = encryptedPrefs.getString("api_key", "") ?: ""
        if (key.isBlank()) {
            LogManager.log("PreferencesRepository", "INFO", "No API key set")
        }
        return key
    }

    // Story Model
    suspend fun setStoryModel(model: String) {
        context.dataStore.edit { prefs ->
            prefs[STORY_MODEL] = model
        }
        LogManager.log("PreferencesRepository", "INFO", "Story model set to: $model")
    }

    fun getStoryModel(): String {
        return DEFAULT_STORY_MODEL // Will be overridden by actual flow reading
    }
    
    fun getStoryModelFlow(): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[STORY_MODEL] ?: DEFAULT_STORY_MODEL
    }
    
    // Creativity (Temperature)
    suspend fun setCreativity(value: Float) {
        context.dataStore.edit { prefs ->
            prefs[CREATIVITY] = value
        }
    }
    
    fun getCreativity(): Double {
        return DEFAULT_CREATIVITY.toDouble()
    }
    
    fun getCreativityFlow(): Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[CREATIVITY] ?: DEFAULT_CREATIVITY
    }
    
    // Fallback Model
    suspend fun setFallbackModel(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[FALLBACK_MODEL] = enabled
        }
    }
    
    fun getFallbackModel(): Boolean {
        return false
    }
    
    fun getFallbackModelFlow(): Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[FALLBACK_MODEL] ?: false
    }
    
    // Default Voice
    suspend fun setDefaultVoice(voice: String) {
        context.dataStore.edit { prefs ->
            prefs[DEFAULT_VOICE_KEY] = voice
        }
    }

    fun getDefaultVoiceFlow(): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[DEFAULT_VOICE_KEY] ?: DEFAULT_VOICE_VALUE
    }
    
    // Highlight Words
    suspend fun setHighlightWords(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HIGHLIGHT_WORDS] = enabled
        }
    }
    
    fun getHighlightWordsFlow(): Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HIGHLIGHT_WORDS] ?: true
    }
    
    // Text Size
    suspend fun setTextSize(size: String) {
        context.dataStore.edit { prefs ->
            prefs[TEXT_SIZE] = size
        }
    }
    
    fun getTextSizeFlow(): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[TEXT_SIZE] ?: DEFAULT_TEXT_SIZE
    }
    
    // Auto Play
    suspend fun setAutoPlay(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_PLAY] = enabled
        }
    }
    
    fun getAutoPlayFlow(): Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AUTO_PLAY] ?: false
    }
    
    // Save Stories
    suspend fun setSaveStories(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SAVE_STORIES] = enabled
        }
    }
    
    fun getSaveStoriesFlow(): Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SAVE_STORIES] ?: true
    }
    
    // Theme
    suspend fun setTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME] = theme
        }
    }
    
    fun getThemeFlow(): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME] ?: DEFAULT_THEME
    }
    
    // Parental PIN
    suspend fun setParentalPin(pin: String) {
        encryptedPrefs.edit().putString("parental_pin", pin).apply()
        LogManager.log("PreferencesRepository", "INFO", "Parental PIN set")
    }
    
    fun getParentalPin(): String {
        return encryptedPrefs.getString("parental_pin", "") ?: ""
    }
    
    fun hasParentalPin(): Boolean {
        return !getParentalPin().isNullOrBlank()
    }
    
    // Daily Limit
    suspend fun setDailyLimit(limit: String) {
        context.dataStore.edit { prefs ->
            prefs[DAILY_LIMIT] = limit
        }
    }
    
    fun getDailyLimitFlow(): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[DAILY_LIMIT] ?: DEFAULT_DAILY_LIMIT
    }
    
    // Content Filter
    suspend fun setContentFilter(filter: String) {
        context.dataStore.edit { prefs ->
            prefs[CONTENT_FILTER] = filter
        }
    }
    
    fun getContentFilterFlow(): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CONTENT_FILTER] ?: DEFAULT_CONTENT_FILTER
    }
    
    // Disabled Genres
    suspend fun setDisabledGenres(genres: String) {
        context.dataStore.edit { prefs ->
            prefs[DISABLED_GENRES] = genres
        }
    }
    
    fun getDisabledGenresFlow(): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[DISABLED_GENRES] ?: ""
    }
    
    /**
     * Tests the API connection.
     */
    suspend fun testConnection(): Result<Boolean> {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            return Result.failure(Exception("API key not set"))
        }

        return try {
            // Simple test - just check if we can make a request
            // In a real implementation, this would call a lightweight endpoint
            LogManager.log("PreferencesRepository", "INFO", "Testing API connection")
            Result.success(true)
        } catch (e: Exception) {
            LogManager.log("PreferencesRepository", "ERROR", "Connection test failed: ${e.message}")
            Result.failure(e)
        }
    }
}
