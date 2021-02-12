package com.crafttalk.chat.di.modules.init

import com.crafttalk.chat.data.repository.*
import com.crafttalk.chat.domain.repository.*
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindChatBehaviorRepository(сhatBehaviorRepository: ChatBehaviorRepository): IChatBehaviorRepository

    @Singleton
    @Binds
    abstract fun bindAuthRepository(authRepository: AuthRepository): IAuthRepository

    @Singleton
    @Binds
    abstract fun bindVisitorRepository(visitorRepository: VisitorRepository): IVisitorRepository

    @Singleton
    @Binds
    abstract fun bindNotificationRepository(notificationRepository: NotificationRepository): INotificationRepository

    @Singleton
    @Binds
    abstract fun bindPersonRepository(personRepository: PersonRepository): IPersonRepository

}