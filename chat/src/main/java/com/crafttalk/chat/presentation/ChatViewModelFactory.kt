package com.crafttalk.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.domain.interactors.*

@Suppress("UNCHECKED_CAST")
class ChatViewModelFactory constructor(
    private val visitor: Visitor?,
    private val view: ChatView,
    private val socketApi: SocketApi,
    private val authInteractor: AuthInteractor,
    private val chatMessageInteractor: ChatMessageInteractor,
    private val notificationInteractor: NotificationInteractor,
    private val fileInteractor: FileInteractor,
    private val customizingChatBehaviorInteractor: CustomizingChatBehaviorInteractor
): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChatViewModel(
            visitor,
            view,
            socketApi,
            authInteractor,
            chatMessageInteractor,
            notificationInteractor,
            fileInteractor,
            customizingChatBehaviorInteractor
        ) as T
    }
}