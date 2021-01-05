package com.crafttalk.chat.di.modules.chat

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.crafttalk.chat.di.ChatScope
import com.crafttalk.chat.domain.entity.auth.Visitor
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
        visitor: Visitor?,
        authChatInteractor: AuthChatInteractor,
        chatMessageInteractor: ChatMessageInteractor,
        notificationInteractor: NotificationInteractor,
        fileInteractor: FileInteractor,
        customizingChatBehaviorInteractor: CustomizingChatBehaviorInteractor,
        context: Context
    ): ChatViewModelFactory = ChatViewModelFactory(
        visitor,
        authChatInteractor,
        chatMessageInteractor,
        notificationInteractor,
        fileInteractor,
        customizingChatBehaviorInteractor,
        context
    )

    @Provides
    @ChatScope
    fun provideChatViewModel(
        parentFragment: Fragment,
        chatViewModelFactory: ChatViewModelFactory
    ): ChatViewModel = ViewModelProvider(parentFragment, chatViewModelFactory).get(ChatViewModel::class.java)

}