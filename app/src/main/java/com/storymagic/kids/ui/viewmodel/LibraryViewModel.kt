package com.storymagic.kids.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storymagic.kids.data.local.StoryEntity
import com.storymagic.kids.data.repository.StoryRepository
import com.storymagic.kids.domain.LogManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Story Library screen.
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val storyRepository: StoryRepository
) : ViewModel() {
    
    private val _allStories = MutableStateFlow<List<StoryEntity>>(emptyList())
    val allStories: StateFlow<List<StoryEntity>> = _allStories.asStateFlow()
    
    private val _filteredStories = MutableStateFlow<List<StoryEntity>>(emptyList())
    val filteredStories: StateFlow<List<StoryEntity>> = _filteredStories.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _showDeleteConfirmation = MutableStateFlow<StoryEntity?>(null)
    val showDeleteConfirmation: StateFlow<StoryEntity?> = _showDeleteConfirmation.asStateFlow()
    
    val filterOptions = listOf("All", "Favorites ⭐", "Recent")
    
    init {
        LogManager.log("LibraryViewModel", "INFO", "LibraryViewModel initialized")
        loadStories()
    }
    
    private fun loadStories() {
        viewModelScope.launch {
            storyRepository.getAllStories().collect { stories ->
                _allStories.value = stories
                _isLoading.value = false
                applyFilters()
                LogManager.log("LibraryViewModel", "INFO", "Loaded ${stories.size} stories")
            }
        }
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }
    
    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        applyFilters()
    }
    
    private fun applyFilters() {
        var stories = _allStories.value
        
        // Apply search filter
        val query = _searchQuery.value
        if (query.isNotBlank()) {
            stories = stories.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.storyText.contains(query, ignoreCase = true)
            }
        }
        
        // Apply category filter
        when (_selectedFilter.value) {
            "Favorites ⭐" -> {
                stories = stories.filter { it.isFavorite }
            }
            "Recent" -> {
                stories = stories.sortedByDescending { it.createdAt }
            }
            "All" -> {
                // No additional filtering
            }
        }
        
        _filteredStories.value = stories
    }
    
    fun toggleFavorite(story: StoryEntity) {
        viewModelScope.launch {
            val updatedStory = story.copy(isFavorite = !story.isFavorite)
            storyRepository.updateStory(updatedStory)
            LogManager.log("LibraryViewModel", "INFO", "Favorite toggled for story: ${story.id}")
        }
    }
    
    fun deleteStory(story: StoryEntity) {
        viewModelScope.launch {
            storyRepository.deleteStory(story)
            _showDeleteConfirmation.value = null
            LogManager.log("LibraryViewModel", "INFO", "Story deleted: ${story.id}")
        }
    }
    
    fun showDeleteConfirmation(story: StoryEntity) {
        _showDeleteConfirmation.value = story
    }
    
    fun hideDeleteConfirmation() {
        _showDeleteConfirmation.value = null
    }
    
    fun getStoryById(storyId: Int): StoryEntity? {
        return _allStories.value.find { it.id == storyId }
    }
}
