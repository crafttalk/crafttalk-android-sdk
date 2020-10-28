package com.crafttalk.chat.di.modules

import com.crafttalk.chat.data.repository.*
import com.crafttalk.chat.domain.repository.*
import dagger.Binds
import dagger.Module

@Module
abstract class RepositoryModule {

    @Binds
    abstract fun bindDataRepository(dataRepository: DataRepository): IDataRepository

    @Binds
    abstract fun bindFileRepository(fileRepository: FileRepository): IFileRepository

    @Binds
    abstract fun bindInternetConnectionRepository(internetConnectionRepository: InternetConnectionRepository): IInternetConnectionRepository

    @Binds
    abstract fun bindMessageRepository(messageRepository: MessageRepository): IMessageRepository

    @Binds
    abstract fun bindVisitorRepository(visitorRepository: VisitorRepository): IVisitorRepository

    @Binds
    abstract fun bindNotificationRepository(notificationRepository: NotificationRepository): INotificationRepository

}