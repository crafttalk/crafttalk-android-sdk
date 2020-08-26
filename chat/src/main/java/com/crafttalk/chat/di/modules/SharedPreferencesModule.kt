package com.crafttalk.chat.di.modules

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SharedPreferencesModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(
        context: Context
    ): SharedPreferences = context.getSharedPreferences("data_visitor", Context.MODE_PRIVATE)

}