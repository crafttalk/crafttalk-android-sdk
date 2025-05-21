package com.crafttalk.chat.di

import android.content.Context
import com.crafttalk.chat.di.modules.init.SharedPreferencesModule
import com.crafttalk.chat.di.modules.init.DBModule
import com.crafttalk.chat.di.modules.init.GsonModule
import com.crafttalk.chat.di.modules.init.NetworkModule
import com.crafttalk.chat.di.modules.init.RepositoryModule
import com.crafttalk.chat.domain.repository.*
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        NetworkModule::class,
        RepositoryModule::class,
        SharedPreferencesModule::class,
        GsonModule::class,
        DBModule::class
    ]
)
interface SdkComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance fun context(context: Context): Builder
        fun build(): SdkComponent
    }
    fun getConditionRepository(): IConditionRepository
    fun getAuthRepository(): IAuthRepository
    fun getVisitorRepository(): IVisitorRepository
    fun getPersonRepository(): IPersonRepository
    fun getNotificationRepository(): INotificationRepository

    fun createChatComponent(): ChatComponent.Builder
}