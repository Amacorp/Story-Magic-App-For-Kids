package com.storymagic.kids.ui.viewmodel

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storymagic.kids.data.local.StoryEntity
import com.storymagic.kids.data.repository.PreferencesRepository
import com.storymagic.kids.data.repository.StoryRepository
import com.storymagic.kids.domain.LogManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel(), TextToSpeech.OnInitListener {
    
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false
    
    private val _currentStory = MutableStateFlow<StoryEntity?>(null)
    val currentStory: StateFlow<StoryEntity?> = _currentStory.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()
    
    private val _selectedVoice = MutableStateFlow("Friendly Mom")
    val selectedVoice: StateFlow<String> = _selectedVoice.asStateFlow()
    
    private val _speechRate = MutableStateFlow(0.8f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()
    
    private val _showCelebration = MutableStateFlow(false)
    val showCelebration: StateFlow<Boolean> = _showCelebration.asStateFlow()
    
    private val _textSize = MutableStateFlow("Medium")
    val textSize: StateFlow<String> = _textSize.asStateFlow()
    
    private val _showVoiceSelector = MutableStateFlow(false)
    val showVoiceSelector: StateFlow<Boolean> = _showVoiceSelector.asStateFlow()

    private val _highlightWords = MutableStateFlow(true)
    val highlightWords: StateFlow<Boolean> = _highlightWords.asStateFlow()
    
    private val _currentWordIndex = MutableStateFlow(-1)
    val currentWordIndex: StateFlow<Int> = _currentWordIndex.asStateFlow()

    private var highlightJob: Job? = null
    private var progressJob: Job? = null
    private var words: List<String> = emptyList()
    private var playbackStartTime: Long = 0

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
    val speedOptions = listOf(0.6f, 0.8f, 1.0f, 1.2f)
    
    init {
        LogManager.log("StoryViewModel", "INFO", "StoryViewModel initialized")
        loadPreferences()
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.getDefaultVoiceFlow().collect { voice ->
                _selectedVoice.value = voice
                LogManager.log("StoryViewModel", "DEBUG", "Default voice loaded: $voice")
            }
        }
        
        viewModelScope.launch {
            preferencesRepository.getTextSizeFlow().collect { size ->
                _textSize.value = size
            }
        }
        
        viewModelScope.launch {
            preferencesRepository.getHighlightWordsFlow().collect { highlight ->
                _highlightWords.value = highlight
                LogManager.log("StoryViewModel", "DEBUG", "Highlight words: $highlight")
            }
        }
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            LogManager.log("StoryViewModel", "INFO", "TTS initialized successfully")
            
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isPlaying.value = true
                    playbackStartTime = System.currentTimeMillis()
                    LogManager.log("StoryViewModel", "DEBUG", "TTS playback started")
                    startWordHighlighting()
                }
                
                override fun onDone(utteranceId: String?) {
                    _isPlaying.value = false
                    _playbackProgress.value = 1f
                    _currentWordIndex.value = -1
                    _showCelebration.value = true
                    highlightJob?.cancel()
                    progressJob?.cancel()
                    LogManager.log("StoryViewModel", "INFO", "TTS playback completed")
                    
                    viewModelScope.launch {
                        delay(3000)
                        _showCelebration.value = false
                    }
                }
                
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isPlaying.value = false
                    highlightJob?.cancel()
                    LogManager.log("StoryViewModel", "ERROR", "TTS playback error")
                }
            })
        } else {
            LogManager.log("StoryViewModel", "ERROR", "TTS initialization failed")
        }
    }
    
    fun initializeTts(context: Context) {
        try {
            textToSpeech = TextToSpeech(context, this)
            LogManager.log("StoryViewModel", "INFO", "TTS initialization started")
        } catch (e: Exception) {
            LogManager.log("StoryViewModel", "ERROR", "TTS initialization exception: ${e.message}")
        }
    }
    
    fun setStory(story: StoryEntity) {
        _currentStory.value = story
        words = story.storyText.split(Regex("\\s+")).filter { it.isNotBlank() }
        LogManager.log("StoryViewModel", "INFO", "Story loaded: ${story.title}, words: ${words.size}")
    }
    
    fun loadStoryById(storyId: Int) {
        viewModelScope.launch {
            val story = storyRepository.getStoryById(storyId)
            if (story != null) {
                _currentStory.value = story
                words = story.storyText.split(Regex("\\s+")).filter { it.isNotBlank() }
                LogManager.log("StoryViewModel", "INFO", "Story loaded from database: ${story.title}, words: ${words.size}")
            } else {
                LogManager.log("StoryViewModel", "ERROR", "Story not found with ID: $storyId")
            }
        }
    }
    
    fun togglePlayPause() {
        if (_isPlaying.value) {
            stopTts()
        } else {
            playStory()
        }
    }
    
    private fun playStory() {
        val story = _currentStory.value ?: return
        
        if (!isTtsInitialized) {
            LogManager.log("StoryViewModel", "WARN", "TTS not initialized")
            return
        }
        
        try {
            applyVoiceSettings()
            
            highlightJob?.cancel()
            progressJob?.cancel()
            
            val utteranceId = "story_utterance"
            textToSpeech?.speak(story.storyText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            LogManager.log("StoryViewModel", "INFO", "TTS play called for story: ${story.title}")
        } catch (e: Exception) {
            LogManager.log("StoryViewModel", "ERROR", "TTS play error: ${e.message}")
        }
    }
    
    private fun startWordHighlighting() {
        if (!_highlightWords.value || words.isEmpty()) return
        
        val totalWords = words.size
        val story = _currentStory.value ?: return
        val totalChars = story.storyText.length
        
        val estimatedDurationMs = (totalChars / 14f * 1000f / _speechRate.value).toLong()
        val wordDurationMs = estimatedDurationMs / totalWords
        
        LogManager.log("StoryViewModel", "DEBUG", "Starting highlight: $totalWords words, ~${wordDurationMs}ms per word")
        
        highlightJob = viewModelScope.launch {
            _currentWordIndex.value = 0
            for (i in 0 until totalWords) {
                if (!_isPlaying.value) break
                _currentWordIndex.value = i
                delay(wordDurationMs.toLong())
            }
        }
        
        progressJob = viewModelScope.launch {
            _playbackProgress.value = 0f
            val startTime = System.currentTimeMillis()
            while (_isPlaying.value && _playbackProgress.value < 0.99f) {
                delay(100)
                val elapsed = System.currentTimeMillis() - startTime
                _playbackProgress.value = (elapsed.toFloat() / estimatedDurationMs).coerceIn(0f, 0.99f)
            }
        }
    }
    
    private fun stopTts() {
        try {
            textToSpeech?.stop()
            highlightJob?.cancel()
            progressJob?.cancel()
            _isPlaying.value = false
            _playbackProgress.value = 0f
            _currentWordIndex.value = -1
            LogManager.log("StoryViewModel", "DEBUG", "TTS stopped")
        } catch (e: Exception) {
            LogManager.log("StoryViewModel", "ERROR", "TTS stop error: ${e.message}")
        }
    }
    
    private fun applyVoiceSettings() {
        val voice = _selectedVoice.value
        val rate = _speechRate.value

        when (voice) {
            "Friendly Mom" -> {
                textToSpeech?.setPitch(1.2f)
                textToSpeech?.setSpeechRate(0.85f * rate)
            }
            "Warm Dad" -> {
                textToSpeech?.setPitch(0.8f)
                textToSpeech?.setSpeechRate(0.85f * rate)
            }
            "Fun Sister" -> {
                textToSpeech?.setPitch(1.35f)
                textToSpeech?.setSpeechRate(0.95f * rate)
            }
            "Cool Brother" -> {
                textToSpeech?.setPitch(0.9f)
                textToSpeech?.setSpeechRate(0.9f * rate)
            }
            "Kind Grandma" -> {
                textToSpeech?.setPitch(1.1f)
                textToSpeech?.setSpeechRate(0.75f * rate)
            }
            "Gentle Grandpa" -> {
                textToSpeech?.setPitch(0.75f)
                textToSpeech?.setSpeechRate(0.75f * rate)
            }
            "Happy Teacher" -> {
                textToSpeech?.setPitch(1.0f)
                textToSpeech?.setSpeechRate(0.9f * rate)
            }
            "Silly Friend" -> {
                textToSpeech?.setPitch(1.4f)
                textToSpeech?.setSpeechRate(1.0f * rate)
            }
        }
        LogManager.log("StoryViewModel", "DEBUG", "Voice settings applied: $voice, rate: $rate")
    }

    fun setVoice(voice: String) {
        _selectedVoice.value = voice
        viewModelScope.launch {
            preferencesRepository.setDefaultVoice(voice)
        }
        
        applyVoiceSettings()
        LogManager.log("StoryViewModel", "DEBUG", "Voice changed to: $voice")
        
        if (_isPlaying.value) {
            LogManager.log("StoryViewModel", "INFO", "Restarting TTS with new voice")
            textToSpeech?.stop()
            viewModelScope.launch {
                delay(100)
                playStory()
            }
        }
    }

    fun cycleSpeed() {
        val currentIndex = speedOptions.indexOf(_speechRate.value)
        val nextIndex = (currentIndex + 1) % speedOptions.size
        _speechRate.value = speedOptions[nextIndex]
        LogManager.log("StoryViewModel", "DEBUG", "Speed changed to: ${_speechRate.value}x")
        
        if (_isPlaying.value) {
            LogManager.log("StoryViewModel", "INFO", "Restarting TTS with new speed")
            textToSpeech?.stop()
            viewModelScope.launch {
                delay(100)
                playStory()
            }
        }
    }
    
    fun toggleVoiceSelector() {
        _showVoiceSelector.value = !_showVoiceSelector.value
    }
    
    fun setHighlightWords(enabled: Boolean) {
        _highlightWords.value = enabled
        viewModelScope.launch {
            preferencesRepository.setHighlightWords(enabled)
        }
        LogManager.log("StoryViewModel", "DEBUG", "Highlight words set to: $enabled")
    }
    
    fun toggleFavorite() {
        viewModelScope.launch {
            val story = _currentStory.value ?: return@launch
            val updatedStory = story.copy(isFavorite = !story.isFavorite)
            storyRepository.updateStory(updatedStory)
            _currentStory.value = updatedStory
            LogManager.log("StoryViewModel", "INFO", "Favorite toggled: ${updatedStory.isFavorite}")
        }
    }

    fun onShareClicked() {
        LogManager.log("StoryViewModel", "INFO", "Story share triggered")
    }

    fun getTextSizeValue(): Float {
        return when (_textSize.value) {
            "Small" -> 16f
            "Large" -> 22f
            else -> 18f
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        highlightJob?.cancel()
        progressJob?.cancel()
        textToSpeech?.shutdown()
        LogManager.log("StoryViewModel", "INFO", "StoryViewModel cleared, TTS shutdown")
    }
}
