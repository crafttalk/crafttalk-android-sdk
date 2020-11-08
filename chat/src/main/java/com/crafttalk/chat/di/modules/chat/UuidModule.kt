package com.crafttalk.chat.di.modules.chat

import com.crafttalk.chat.data.local.pref.Uuid
import com.crafttalk.chat.di.ChatScope
import com.crafttalk.chat.di.Uuid as UuidAnnotation
import dagger.Module
import dagger.Provides

@Module
class UuidModule {

    @UuidAnnotation
    @Provides
    @ChatScope
    fun provideUuid(): String = Uuid.generateUUID(false)

}