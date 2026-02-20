package com.storymagic.kids.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.storymagic.kids.domain.LogManager
import com.storymagic.kids.domain.StoryGenerationData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
) : ViewModel() {
    
    private val _childName = MutableStateFlow("")
    val childName: StateFlow<String> = _childName.asStateFlow()
    
    private val _childAge = MutableStateFlow(5)
    val childAge: StateFlow<Int> = _childAge.asStateFlow()
    
    private val _childGender = MutableStateFlow("Boy")
    val childGender: StateFlow<String> = _childGender.asStateFlow()
    
    private val _selectedGenres = MutableStateFlow<List<String>>(emptyList())
    val selectedGenres: StateFlow<List<String>> = _selectedGenres.asStateFlow()
    
    private val _belovedObject = MutableStateFlow("")
    val belovedObject: StateFlow<String> = _belovedObject.asStateFlow()
    
    private val _includePet = MutableStateFlow(false)
    val includePet: StateFlow<Boolean> = _includePet.asStateFlow()
    
    private val _selectedPetType = MutableStateFlow<String?>(null)
    val selectedPetType: StateFlow<String?> = _selectedPetType.asStateFlow()
    
    private val _petName = MutableStateFlow("")
    val petName: StateFlow<String> = _petName.asStateFlow()
    
    private val _selectedMoral = MutableStateFlow("Friendship")
    val selectedMoral: StateFlow<String> = _selectedMoral.asStateFlow()
    
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()
    
    private val _navigateToLoading = MutableStateFlow(false)
    val navigateToLoading: StateFlow<Boolean> = _navigateToLoading.asStateFlow()
    
    fun setChildName(name: String) {
        _childName.value = name
    }
    
    fun setChildAge(age: Int) {
        _childAge.value = age
    }
    
    fun setChildGender(gender: String) {
        _childGender.value = gender
    }
    
    fun toggleGenre(genre: String) {
        val current = _selectedGenres.value
        _selectedGenres.value = if (genre in current) {
            current - genre
        } else {
            current + genre
        }
        LogManager.log("OnboardingViewModel", "DEBUG", "Genre toggled: $genre, selected: ${_selectedGenres.value}")
    }
    
    fun setBelovedObject(obj: String) {
        _belovedObject.value = obj
    }
    
    fun setIncludePet(include: Boolean) {
        _includePet.value = include
        if (!include) {
            _selectedPetType.value = null
            _petName.value = ""
        }
    }
    
    fun setPetType(type: String?) {
        _selectedPetType.value = type
    }
    
    fun setPetName(name: String) {
        _petName.value = name
    }
    
    fun setMoral(moral: String) {
        _selectedMoral.value = moral
    }
    
    fun setCurrentStep(step: Int) {
        _currentStep.value = step
    }
    
    fun nextStep() {
        _currentStep.value = (_currentStep.value + 1).coerceIn(0, 3)
    }
    
    fun previousStep() {
        _currentStep.value = (_currentStep.value - 1).coerceIn(0, 3)
    }
    
    fun prepareStoryGeneration() {
        LogManager.log("OnboardingViewModel", "INFO", "========== PREPARING STORY GENERATION ==========")
        LogManager.log("OnboardingViewModel", "INFO", "Name: '${_childName.value}', Age: ${_childAge.value}, Gender: ${_childGender.value}")
        LogManager.log("OnboardingViewModel", "INFO", "Genres: ${_selectedGenres.value.joinToString(", ")}")
        LogManager.log("OnboardingViewModel", "INFO", "Beloved Object: '${_belovedObject.value}'")
        LogManager.log("OnboardingViewModel", "INFO", "Moral: '${_selectedMoral.value}'")
        
        StoryGenerationData.childName = _childName.value.ifBlank { "Little Explorer" }
        StoryGenerationData.childAge = _childAge.value
        StoryGenerationData.childGender = _childGender.value
        StoryGenerationData.genres = _selectedGenres.value.ifEmpty { listOf("Adventure") }
        StoryGenerationData.belovedObject = _belovedObject.value.ifBlank { "a magical surprise" }
        StoryGenerationData.petType = _selectedPetType.value
        StoryGenerationData.petName = _petName.value.ifBlank { null }
        StoryGenerationData.moral = _selectedMoral.value
        StoryGenerationData.isGenerating = true
        StoryGenerationData.storyGenerated = false
        StoryGenerationData.errorMessage = null
        
        LogManager.log("OnboardingViewModel", "INFO", "Data stored in StoryGenerationData")
        
        _navigateToLoading.value = true
    }
    
    fun onNavigatedToLoading() {
        _navigateToLoading.value = false
    }
    
    fun isCurrentStepValid(): Boolean {
        return when (_currentStep.value) {
            0 -> _childName.value.isNotBlank()
            1 -> true
            2 -> true
            3 -> true
            else -> false
        }
    }
    
    fun resetState() {
        LogManager.log("OnboardingViewModel", "INFO", "Resetting onboarding state")
        _childName.value = ""
        _childAge.value = 5
        _childGender.value = "Boy"
        _selectedGenres.value = emptyList()
        _belovedObject.value = ""
        _includePet.value = false
        _selectedPetType.value = null
        _petName.value = ""
        _selectedMoral.value = "Friendship"
        _currentStep.value = 0
        _navigateToLoading.value = false
    }
}
