package com.storymagic.kids.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.storymagic.kids.domain.LogManager

@Database(
    entities = [StoryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class StoryDatabase : RoomDatabase() {
    
    abstract fun storyDao(): StoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: StoryDatabase? = null
        
        fun getDatabase(context: Context): StoryDatabase {
            LogManager.log("StoryDatabase", "INFO", "Getting database instance")
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StoryDatabase::class.java,
                    "storymagic_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                LogManager.log("StoryDatabase", "INFO", "Database instance created")
                instance
            }
        }
    }
}
