package com.crafttalk.chat.di.modules.chat

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.crafttalk.chat.di.ChatScope
import com.crafttalk.chat.domain.interactors.*
import com.crafttalk.chat.presentation.ChatViewModel
import com.crafttalk.chat.presentation.ChatViewModelFactory
import dagger.Module
import dagger.Provides

@Module
class ViewModelModule {

    @Provides
    @ChatScope
    fun provideChatViewModelFactory(
        authChatInteractor: AuthInteractor,
        chatMessageInteractor: ChatMessageInteractor,
        fileInteractor: FileInteractor,
        conditionInteractor: ConditionInteractor,
        context: Context
    ): ChatViewModelFactory = ChatViewModelFactory(
        authChatInteractor,
        chatMessageInteractor,
        fileInteractor,
        conditionInteractor,
        context
    )

    @Provides
    @ChatScope
    fun provideChatViewModel(
        parentFragment: Fragment,
        chatViewModelFactory: ChatViewModelFactory
    ): ChatViewModel = ViewModelProvider(parentFragment, chatViewModelFactory).get(ChatViewModel::class.java)

}