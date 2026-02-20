package com.storymagic.kids.domain

data object StoryGenerationData {
    var childName: String = ""
    var childAge: Int = 5
    var childGender: String = "Boy"
    var genres: List<String> = emptyList()
    var belovedObject: String = ""
    var petType: String? = null
    var petName: String? = null
    var moral: String = "Friendship"
    var isGenerating: Boolean = false
    var storyGenerated: Boolean = false
    var errorMessage: String? = null
    
    fun isValid(): Boolean {
        return childName.isNotBlank() && childAge in 3..12 && genres.isNotEmpty()
    }
    
    fun markAsGenerated() {
        storyGenerated = true
        isGenerating = false
        errorMessage = null
    }
    
    fun markAsError(message: String) {
        errorMessage = message
        isGenerating = false
        storyGenerated = false
    }
    
    fun reset() {
        childName = ""
        childAge = 5
        childGender = "Boy"
        genres = emptyList()
        belovedObject = ""
        petType = null
        petName = null
        moral = "Friendship"
        isGenerating = false
        storyGenerated = false
        errorMessage = null
    }
}
