package com.storymagic.kids.di

import android.content.Context
import com.storymagic.kids.data.local.StoryDao
import com.storymagic.kids.data.local.StoryDatabase
import com.storymagic.kids.data.remote.OpenRouterApi
import com.storymagic.kids.data.repository.PreferencesRepository
import com.storymagic.kids.data.repository.StoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesRepository(@ApplicationContext context: Context): PreferencesRepository {
        return PreferencesRepository(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkDiModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): okhttp3.OkHttpClient {
        return com.storymagic.kids.data.remote.NetworkModule.provideOkHttpClient()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: okhttp3.OkHttpClient): retrofit2.Retrofit {
        return com.storymagic.kids.data.remote.NetworkModule.provideRetrofit(okHttpClient)
    }
    
    @Provides
    @Singleton
    fun provideOpenRouterApi(retrofit: retrofit2.Retrofit): OpenRouterApi {
        return com.storymagic.kids.data.remote.NetworkModule.provideOpenRouterApi(retrofit)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideStoryDatabase(@ApplicationContext context: Context): StoryDatabase {
        return StoryDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideStoryDao(database: StoryDatabase): StoryDao {
        return database.storyDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideStoryRepository(
        openRouterApi: OpenRouterApi,
        storyDao: StoryDao,
        preferencesRepository: PreferencesRepository
    ): StoryRepository {
        return StoryRepository(openRouterApi, storyDao, preferencesRepository)
    }
}
