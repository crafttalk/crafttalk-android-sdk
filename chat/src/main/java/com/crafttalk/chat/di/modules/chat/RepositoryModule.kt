package com.crafttalk.chat.di.modules.chat

import com.crafttalk.chat.data.repository.*
import com.crafttalk.chat.domain.repository.*
import dagger.Binds
import dagger.Module

@Module
abstract class RepositoryModule {

    @Binds
    abstract fun bindFileRepository(fileRepository: FileRepository): IFileRepository

    @Binds
    abstract fun bindMessageRepository(messageRepository: MessageRepository): IMessageRepository

    @Binds
    abstract fun bindCacheRepository(cacheRepository: CacheRepository): ICacheRepository

}