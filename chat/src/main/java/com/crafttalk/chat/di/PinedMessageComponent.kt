package com.crafttalk.chat.di

import com.crafttalk.chat.di.modules.chat.NetworkModule
import com.crafttalk.chat.di.modules.chat.RepositoryModule
import com.crafttalk.chat.di.modules.chat.ViewModelModule
//import com.crafttalk.chat.presentation.feature.pined_message_viewer.PinedMessage
import dagger.Subcomponent


@ChatScope
@Subcomponent(
    modules = [
        NetworkModule::class,
        RepositoryModule::class,
        ViewModelModule::class
    ]
)
interface PinedMessageComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): PinedMessageComponent
    }
    //fun inject(pinedMessage: PinedMessage)
}