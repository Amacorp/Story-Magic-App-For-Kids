package com.storymagic.kids.ui.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storymagic.kids.data.repository.PreferencesRepository
import com.storymagic.kids.domain.LogManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    
    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()
    
    private val _isApiKeyVisible = MutableStateFlow(false)
    val isApiKeyVisible: StateFlow<Boolean> = _isApiKeyVisible.asStateFlow()
    
    private val _connectionTestResult = MutableStateFlow<TestResult?>(null)
    val connectionTestResult: StateFlow<TestResult?> = _connectionTestResult.asStateFlow()
    
    private val _selectedStoryModel = MutableStateFlow("google/gemini-2.0-flash-lite-001")
    val selectedStoryModel: StateFlow<String> = _selectedStoryModel.asStateFlow()
    
    private val _creativity = MutableStateFlow(0.8f)
    val creativity: StateFlow<Float> = _creativity.asStateFlow()
    
    private val _fallbackModel = MutableStateFlow(false)
    val fallbackModel: StateFlow<Boolean> = _fallbackModel.asStateFlow()
    
    private val _defaultVoice = MutableStateFlow("Friendly Female")
    val defaultVoice: StateFlow<String> = _defaultVoice.asStateFlow()
    
    private val _highlightWords = MutableStateFlow(false)
    val highlightWords: StateFlow<Boolean> = _highlightWords.asStateFlow()
    
    private val _textSize = MutableStateFlow("Medium")
    val textSize: StateFlow<String> = _textSize.asStateFlow()
    
    private val _autoPlay = MutableStateFlow(false)
    val autoPlay: StateFlow<Boolean> = _autoPlay.asStateFlow()
    
    private val _saveStories = MutableStateFlow(true)
    val saveStories: StateFlow<Boolean> = _saveStories.asStateFlow()
    
    private val _theme = MutableStateFlow("Light")
    val theme: StateFlow<String> = _theme.asStateFlow()
    
    private val _hasPin = MutableStateFlow(false)
    val hasPin: StateFlow<Boolean> = _hasPin.asStateFlow()
    
    private val _showPinDialog = MutableStateFlow(false)
    val showPinDialog: StateFlow<Boolean> = _showPinDialog.asStateFlow()
    
    private val _dailyLimit = MutableStateFlow("10")
    val dailyLimit: StateFlow<String> = _dailyLimit.asStateFlow()
    
    private val _contentFilter = MutableStateFlow("Moderate")
    val contentFilter: StateFlow<String> = _contentFilter.asStateFlow()
    
    private val _logEntries = MutableStateFlow(0)
    val logEntries: StateFlow<Int> = _logEntries.asStateFlow()
    
    val storyModelOptions = listOf(
        "google/gemini-2.0-flash-lite-001",
        "google/gemini-2.0-flash-001",
        "deepseek/deepseek-chat-v3-0324:free",
        "meta-llama/llama-3.1-8b-instruct:free"
    )
    
    val voiceOptions = listOf(
        "Friendly Mom",
        "Warm Dad",
        "Fun Sister",
        "Cool Brother",
        "Kind Grandma",
        "Gentle Grandpa",
        "Happy Teacher",
        "Silly Friend"
    )
    val textSizeOptions = listOf("Small", "Medium", "Large")
    val themeOptions = listOf("Light", "Dark", "Auto")
    val contentFilterOptions = listOf("Relaxed", "Moderate", "Strict")
    
    data class TestResult(val success: Boolean, val message: String)
    
    init {
        LogManager.log("SettingsViewModel", "INFO", "SettingsViewModel initialized")
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.getStoryModelFlow().collect { model ->
                _selectedStoryModel.value = model
            }
        }

        viewModelScope.launch {
            preferencesRepository.getCreativityFlow().collect { value ->
                _creativity.value = value
            }
        }

        viewModelScope.launch {
            preferencesRepository.getFallbackModelFlow().collect { enabled ->
                _fallbackModel.value = enabled
            }
        }

        viewModelScope.launch {
            preferencesRepository.getDefaultVoiceFlow().collect { voice ->
                _defaultVoice.value = voice
            }
        }

        viewModelScope.launch {
            preferencesRepository.getHighlightWordsFlow().collect { enabled ->
                _highlightWords.value = enabled
            }
        }

        viewModelScope.launch {
            preferencesRepository.getTextSizeFlow().collect { size ->
                _textSize.value = size
            }
        }

        viewModelScope.launch {
            preferencesRepository.getAutoPlayFlow().collect { enabled ->
                _autoPlay.value = enabled
            }
        }

        viewModelScope.launch {
            preferencesRepository.getSaveStoriesFlow().collect { enabled ->
                _saveStories.value = enabled
            }
        }

        viewModelScope.launch {
            preferencesRepository.getThemeFlow().collect { theme ->
                _theme.value = theme
            }
        }

        viewModelScope.launch {
            preferencesRepository.getDailyLimitFlow().collect { limit ->
                _dailyLimit.value = limit
            }
        }

        viewModelScope.launch {
            preferencesRepository.getContentFilterFlow().collect { filter ->
                _contentFilter.value = filter
            }
        }

        _hasPin.value = preferencesRepository.hasParentalPin()
    }
    
    fun setApiKey(key: String) {
        _apiKey.value = key
    }
    
    fun loadApiKey() {
        _apiKey.value = preferencesRepository.getApiKey()
        LogManager.log("SettingsViewModel", "INFO", "API Key loaded: ${_apiKey.value.take(12)}...")
    }
    
    fun toggleApiKeyVisibility() {
        _isApiKeyVisible.value = !_isApiKeyVisible.value
    }
    
    fun saveApiKey() {
        viewModelScope.launch {
            preferencesRepository.setApiKey(_apiKey.value)
            LogManager.log("SettingsViewModel", "INFO", "API key saved")
        }
    }
    
    fun testConnection() {
        viewModelScope.launch {
            _connectionTestResult.value = null
            val result = preferencesRepository.testConnection()
            _connectionTestResult.value = if (result.isSuccess) {
                LogManager.log("SettingsViewModel", "INFO", "Connection test successful")
                TestResult(true, "Connection successful! API is working.")
            } else {
                LogManager.log("SettingsViewModel", "ERROR", "Connection test failed")
                TestResult(false, "Connection failed. Please check your API key.")
            }
        }
    }
    
    fun setStoryModel(model: String) {
        _selectedStoryModel.value = model
        viewModelScope.launch {
            preferencesRepository.setStoryModel(model)
        }
        LogManager.log("SettingsViewModel", "DEBUG", "Story model set: $model")
    }
    
    fun setCreativity(value: Float) {
        _creativity.value = value
        viewModelScope.launch {
            preferencesRepository.setCreativity(value)
        }
    }
    
    fun setFallbackModel(enabled: Boolean) {
        _fallbackModel.value = enabled
        viewModelScope.launch {
            preferencesRepository.setFallbackModel(enabled)
        }
    }
    
    fun setDefaultVoice(voice: String) {
        _defaultVoice.value = voice
        viewModelScope.launch {
            preferencesRepository.setDefaultVoice(voice)
        }
    }
    
    fun setHighlightWords(enabled: Boolean) {
        _highlightWords.value = enabled
        viewModelScope.launch {
            preferencesRepository.setHighlightWords(enabled)
        }
    }
    
    fun setTextSize(size: String) {
        _textSize.value = size
        viewModelScope.launch {
            preferencesRepository.setTextSize(size)
        }
    }
    
    fun setAutoPlay(enabled: Boolean) {
        _autoPlay.value = enabled
        viewModelScope.launch {
            preferencesRepository.setAutoPlay(enabled)
        }
    }
    
    fun setSaveStories(enabled: Boolean) {
        _saveStories.value = enabled
        viewModelScope.launch {
            preferencesRepository.setSaveStories(enabled)
        }
    }
    
    fun setTheme(theme: String) {
        _theme.value = theme
        viewModelScope.launch {
            preferencesRepository.setTheme(theme)
        }
    }
    
    fun setPin(pin: String) {
        viewModelScope.launch {
            preferencesRepository.setParentalPin(pin)
            _hasPin.value = true
            LogManager.log("SettingsViewModel", "INFO", "Parental PIN set")
        }
    }
    
    fun verifyPin(pin: String): Boolean {
        return preferencesRepository.getParentalPin() == pin
    }
    
    fun setDailyLimit(limit: String) {
        _dailyLimit.value = limit
        viewModelScope.launch {
            preferencesRepository.setDailyLimit(limit)
        }
    }
    
    fun setContentFilter(filter: String) {
        _contentFilter.value = filter
        viewModelScope.launch {
            preferencesRepository.setContentFilter(filter)
        }
    }
    
    fun updateLogCount() {
        _logEntries.value = LogManager.getLogCount()
    }
    
    fun copyLogs(context: Context): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("StoryMagic Logs", LogManager.getFormattedLogs())
            clipboard.setPrimaryClip(clip)
            LogManager.log("SettingsViewModel", "INFO", "Logs copied to clipboard")
            true
        } catch (e: Exception) {
            LogManager.log("SettingsViewModel", "ERROR", "Failed to copy logs: ${e.message}")
            false
        }
    }
    
    fun clearLogs() {
        LogManager.clearLogs()
        _logEntries.value = 0
    }

    fun shareLogs(context: Context): Intent? {
        return try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "StoryMagic Kids - Debug Logs")
                putExtra(Intent.EXTRA_TEXT, LogManager.getFormattedLogs())
            }
            LogManager.log("SettingsViewModel", "INFO", "Share intent created")
            shareIntent
        } catch (e: Exception) {
            LogManager.log("SettingsViewModel", "ERROR", "Failed to create share intent: ${e.message}")
            null
        }
    }
}
