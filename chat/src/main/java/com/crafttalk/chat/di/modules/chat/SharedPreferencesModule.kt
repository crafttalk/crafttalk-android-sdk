package com.crafttalk.chat.di.modules.chat

import android.content.Context
import android.content.SharedPreferences
import com.crafttalk.chat.di.ChatScope
import dagger.Module
import dagger.Provides

@Module
class SharedPreferencesModule {

    @Provides
    @ChatScope
    fun provideSharedPreferences(
        context: Context
    ): SharedPreferences = context.getSharedPreferences("data_visitor", Context.MODE_PRIVATE)

}