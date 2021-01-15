package com.crafttalk.chat.di

import androidx.fragment.app.Fragment
import com.crafttalk.chat.di.modules.chat.*
import com.crafttalk.chat.presentation.ChatView
import dagger.BindsInstance
import dagger.Subcomponent

@ChatScope
@Subcomponent(
    modules = [
        NetworkModule::class,
        RepositoryModule::class,
        ViewModelModule::class
    ]
)
interface ChatComponent {
    @Subcomponent.Builder
    interface Builder {
        @BindsInstance fun parentFragment(parentFragment: Fragment): Builder
        fun build(): ChatComponent
    }
    fun inject(chatView: ChatView)
}