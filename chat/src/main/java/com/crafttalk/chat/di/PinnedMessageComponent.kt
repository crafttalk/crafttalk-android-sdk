package com.crafttalk.chat.di

import com.crafttalk.chat.di.modules.chat.NetworkModule
import com.crafttalk.chat.di.modules.chat.RepositoryModule
import com.crafttalk.chat.di.modules.chat.ViewModelModule
//import com.crafttalk.chat.presentation.feature.pinned_message_viewer.PinedMessage
import dagger.Subcomponent


@ChatScope
@Subcomponent(
    modules = [
        NetworkModule::class,
        RepositoryModule::class,
        ViewModelModule::class
    ]
)
interface PinnedMessageComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): PinnedMessageComponent
    }
    //fun inject(pinedMessage: PinedMessage)
}