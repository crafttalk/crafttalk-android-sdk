package com.crafttalk.chat.di.modules.init

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SharedPreferencesModule {
    private val Context.dataStore by preferencesDataStore(name = "crafttalkChatInfo")

    @Provides
    @Singleton
    fun provideSharedPreferences(
        context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }
}