package com.storymagic.kids.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val childName: String,
    val childAge: Int,
    val childGender: String = "Boy",
    val genres: String,
    val belovedObject: String,
    val petType: String?,
    val petName: String?,
    val moral: String,
    val title: String,
    val description: String,
    val storyText: String,
    val imageUrl: String?,
    val imageDescription: String?,
    val gradientColorStart: String = "#FF6B9D",
    val gradientColorEnd: String = "#00D4FF",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val readingTimeMinutes: Int
)
