package com.crafttalk.chat.di

import android.content.Context
import com.crafttalk.chat.di.modules.init.DBModule
import com.crafttalk.chat.di.modules.init.GsonModule
import com.crafttalk.chat.di.modules.init.NetworkModule
import com.crafttalk.chat.di.modules.init.RepositoryModule
import com.crafttalk.chat.domain.repository.IChatBehaviorRepository
import com.crafttalk.chat.domain.repository.INotificationRepository
import com.crafttalk.chat.domain.repository.IVisitorRepository
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        NetworkModule::class,
        RepositoryModule::class,
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
    fun getChatBehaviorRepository(): IChatBehaviorRepository
    fun getVisitorRepository(): IVisitorRepository
    fun getNotificationRepository(): INotificationRepository

    fun createChatComponent(): ChatComponent.Builder
}