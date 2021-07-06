package com.crafttalk.chat.domain.interactors

import com.crafttalk.chat.domain.repository.IChatBehaviorRepository
import com.crafttalk.chat.initialization.ChatMessageListener
import com.crafttalk.chat.presentation.ChatInternetConnectionListener
import com.crafttalk.chat.utils.ChatStatus
import javax.inject.Inject

class CustomizingChatBehaviorInteractor
@Inject constructor(
    private val chatBehaviorRepository: IChatBehaviorRepository
) {

    fun setInternetConnectionListener(listener: ChatInternetConnectionListener) {
        chatBehaviorRepository.setInternetConnectionListener(listener)
    }

    fun setMessageListener(listener: ChatMessageListener) {
        chatBehaviorRepository.setMessageListener(listener)
    }

    fun leaveChatScreen() {
        chatBehaviorRepository.setStatusChat(ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP)
    }

    fun goToChatScreen() {
        chatBehaviorRepository.setStatusChat(ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP)
    }

    fun openApp() {
        chatBehaviorRepository.setStatusChat(
            when (chatBehaviorRepository.getStatusChat()) {
                ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP, ChatStatus.ON_CHAT_SCREEN_BACKGROUND_APP -> ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP
                ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP, ChatStatus.NOT_ON_CHAT_SCREEN_BACKGROUND_APP -> ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP
            }
        )
    }

    fun closeApp() {
        chatBehaviorRepository.setStatusChat(
            when (chatBehaviorRepository.getStatusChat()) {
                ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP, ChatStatus.ON_CHAT_SCREEN_BACKGROUND_APP -> ChatStatus.ON_CHAT_SCREEN_BACKGROUND_APP
                ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP, ChatStatus.NOT_ON_CHAT_SCREEN_BACKGROUND_APP -> ChatStatus.NOT_ON_CHAT_SCREEN_BACKGROUND_APP
            }
        )
    }

    fun createSessionChat() {
        chatBehaviorRepository.createSessionChat()
    }

    fun destroySessionChat() {
        chatBehaviorRepository.destroySessionChat()
    }

    fun dropChat() {
        chatBehaviorRepository.dropChat()
    }

    fun giveFeedbackOnOperator(countStars: Int) {
        chatBehaviorRepository.giveFeedbackOnOperator(countStars)
    }

}