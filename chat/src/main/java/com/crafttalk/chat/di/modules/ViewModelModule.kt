package com.crafttalk.chat.di.modules

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.interactors.*
import com.crafttalk.chat.presentation.ChatView
import com.crafttalk.chat.presentation.ChatViewModel
import com.crafttalk.chat.presentation.ChatViewModelFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ViewModelModule {

    @Provides
    @Singleton
    fun provideChatViewModelFactory(
        visitor: Visitor?,
        view: ChatView,
        socketApi: SocketApi,
        authInteractor: AuthInteractor,
        chatMessageInteractor: ChatMessageInteractor,
        notificationInteractor: NotificationInteractor,
        fileInteractor: FileInteractor,
        customizingChatBehaviorInteractor: CustomizingChatBehaviorInteractor
    ): ChatViewModelFactory = ChatViewModelFactory(
        visitor,
        view,
        socketApi,
        authInteractor,
        chatMessageInteractor,
        notificationInteractor,
        fileInteractor,
        customizingChatBehaviorInteractor
    )

    @Provides
    @Singleton
    fun provideChatViewModel(
        parentFragment: Fragment,
        chatViewModelFactory: ChatViewModelFactory
    ): ChatViewModel = ViewModelProvider(parentFragment, chatViewModelFactory).get(ChatViewModel::class.java)

}