package com.storymagic.kids.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storymagic.kids.data.local.StoryEntity
import com.storymagic.kids.data.repository.StoryRepository
import com.storymagic.kids.domain.LogManager
import com.storymagic.kids.domain.Resource
import com.storymagic.kids.domain.StoryGenerationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoadingViewModel @Inject constructor(
    private val storyRepository: StoryRepository
) : ViewModel() {

    private val _loadingMessageIndex = MutableStateFlow(0)
    val loadingMessageIndex: StateFlow<Int> = _loadingMessageIndex.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _story = MutableStateFlow<StoryEntity?>(null)
    val story: StateFlow<StoryEntity?> = _story.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var generationJob: Job? = null
    private var animationJob: Job? = null

    val loadingMessages = listOf(
        "Gathering magic words... ✨",
        "Painting beautiful pictures... 🎨",
        "Making characters come alive... 🌟",
        "Sprinkling story dust... 💫",
        "Almost ready... 📖"
    )

    init {
        LogManager.log("LoadingViewModel", "INFO", "LoadingViewModel initialized")
        LogManager.log("LoadingViewModel", "INFO", "StoryGenerationData valid: ${StoryGenerationData.isValid()}")
        LogManager.log("LoadingViewModel", "INFO", "Child Name: ${StoryGenerationData.childName}")
        LogManager.log("LoadingViewModel", "INFO", "Child Age: ${StoryGenerationData.childAge}")
        LogManager.log("LoadingViewModel", "INFO", "Genres: ${StoryGenerationData.genres.joinToString(", ")}")
        
        startLoadingAnimation()
        startStoryGeneration()
    }

    private fun startLoadingAnimation() {
        animationJob = viewModelScope.launch {
            var index = 0
            while (_isLoading.value) {
                delay(3000)
                index = (index + 1) % loadingMessages.size
                _loadingMessageIndex.value = index
                _progress.value = ((index + 1).toFloat() / loadingMessages.size).coerceAtMost(0.9f)
            }
        }
    }

    private fun startStoryGeneration() {
        generationJob = viewModelScope.launch {
            LogManager.log("LoadingViewModel", "INFO", "========== STARTING STORY GENERATION ==========")
            
            if (!StoryGenerationData.isValid()) {
                _errorMessage.value = "Invalid story data. Please try again."
                _isLoading.value = false
                LogManager.log("LoadingViewModel", "ERROR", "Invalid StoryGenerationData")
                return@launch
            }

            try {
                storyRepository.generateStory(
                    childName = StoryGenerationData.childName,
                    age = StoryGenerationData.childAge,
                    gender = StoryGenerationData.childGender,
                    genres = StoryGenerationData.genres,
                    belovedObject = StoryGenerationData.belovedObject,
                    petType = StoryGenerationData.petType,
                    petName = StoryGenerationData.petName,
                    moral = StoryGenerationData.moral
                ).collect { resource ->
                    LogManager.log("LoadingViewModel", "INFO", "Resource state: ${resource.javaClass.simpleName}")
                    
                    when (resource) {
                        is Resource.Loading -> {
                            LogManager.log("LoadingViewModel", "INFO", "Story generation in progress...")
                        }
                        is Resource.Success -> {
                            LogManager.log("LoadingViewModel", "INFO", "========== STORY GENERATED SUCCESSFULLY ==========")
                            LogManager.log("LoadingViewModel", "INFO", "Title: ${resource.data.title}")
                            LogManager.log("LoadingViewModel", "INFO", "ID: ${resource.data.id}")
                            LogManager.log("LoadingViewModel", "INFO", "Image URL: ${resource.data.imageUrl}")
                            
                            _story.value = resource.data
                            _progress.value = 1f
                            _isLoading.value = false
                            _isSuccess.value = true
                            
                            StoryGenerationData.markAsGenerated()
                            LogManager.log("LoadingViewModel", "INFO", "Story saved and marked as generated")
                        }
                        is Resource.Error -> {
                            LogManager.log("LoadingViewModel", "ERROR", "Story generation failed: ${resource.message}")
                            _errorMessage.value = resource.message
                            _isLoading.value = false
                            StoryGenerationData.markAsError(resource.message)
                        }
                    }
                }
            } catch (e: Exception) {
                LogManager.log("LoadingViewModel", "ERROR", "Exception during story generation: ${e.message}")
                LogManager.log("LoadingViewModel", "ERROR", "Stack: ${android.util.Log.getStackTraceString(e)}")
                _errorMessage.value = "Something went wrong: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun cancelLoading() {
        generationJob?.cancel()
        animationJob?.cancel()
        _isLoading.value = false
        StoryGenerationData.isGenerating = false
        LogManager.log("LoadingViewModel", "INFO", "Loading cancelled by user")
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        generationJob?.cancel()
        animationJob?.cancel()
    }
}
