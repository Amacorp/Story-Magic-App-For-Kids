package com.storymagic.kids

import android.app.Application
import com.storymagic.kids.domain.LogManager
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for StoryMagic Kids.
 * Initializes Hilt and sets up global components.
 */
@HiltAndroidApp
class StoryMagicApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize LogManager
        LogManager.initialize(filesDir)
        
        LogManager.log("StoryMagicApplication", "INFO", "Application started")
        LogManager.log("StoryMagicApplication", "INFO", "App version: 1.0.0")
        LogManager.log("StoryMagicApplication", "INFO", "Device info: Android ${android.os.Build.VERSION.RELEASE}")
    }
}
