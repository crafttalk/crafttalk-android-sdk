package com.crafttalk.chat.di.modules.chat

import com.crafttalk.chat.data.repository.*
import com.crafttalk.chat.di.ChatScope
import com.crafttalk.chat.domain.repository.*
import dagger.Binds
import dagger.Module

@Module
abstract class RepositoryModule {

    @ChatScope
    @Binds
    abstract fun bindFileRepository(fileRepository: FileRepository): IFileRepository

    @ChatScope
    @Binds
    abstract fun bindMessageRepository(messageRepository: MessageRepository): IMessageRepository

}