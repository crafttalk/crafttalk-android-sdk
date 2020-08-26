package com.crafttalk.chat.di.modules

import com.crafttalk.chat.data.local.pref.Uuid
import com.crafttalk.chat.di.Uuid as UuidAnnotation
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UuidModule {

    @UuidAnnotation
    @Provides
    @Singleton
    fun provideUuid(): String = Uuid.generateUUID(false)

}