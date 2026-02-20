package com.storymagic.kids.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Story operations.
 */
@Dao
interface StoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity): Long
    
    @Update
    suspend fun updateStory(story: StoryEntity)
    
    @Delete
    suspend fun deleteStory(story: StoryEntity)
    
    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    fun getAllStories(): Flow<List<StoryEntity>>
    
    @Query("SELECT * FROM stories WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteStories(): Flow<List<StoryEntity>>
    
    @Query("SELECT * FROM stories WHERE id = :id")
    suspend fun getStoryById(id: Int): StoryEntity?
    
    @Query("SELECT * FROM stories WHERE title LIKE '%' || :query || '%' OR storyText LIKE '%' || :query || '%'")
    fun searchStories(query: String): Flow<List<StoryEntity>>
    
    @Query("SELECT COUNT(*) FROM stories WHERE createdAt > :startOfDay")
    suspend fun getStoryCountToday(startOfDay: Long): Int
    
    @Query("SELECT * FROM stories WHERE id = :id")
    fun getStoryByIdFlow(id: Int): Flow<StoryEntity?>
}
