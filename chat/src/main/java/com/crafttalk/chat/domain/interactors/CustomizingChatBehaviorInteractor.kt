package com.crafttalk.chat.domain.interactors

import com.crafttalk.chat.domain.repository.IChatBehaviorRepository
import com.crafttalk.chat.initialization.ChatMessageListener
import com.crafttalk.chat.presentation.ChatInternetConnectionListener
import com.crafttalk.chat.utils.ChatStatus
import javax.inject.Inject

class CustomizingChatBehaviorInteractor
@Inject constructor(
    private val сhatBehaviorRepository: IChatBehaviorRepository
) {

    fun setInternetConnectionListener(listener: ChatInternetConnectionListener) {
        сhatBehaviorRepository.setInternetConnectionListener(listener)
    }

    fun setMessageListener(listener: ChatMessageListener) {
        сhatBehaviorRepository.setMessageListener(listener)
    }

    fun leaveChatScreen() {
        сhatBehaviorRepository.setStatusChat(ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP)
    }

    fun goToChatScreen() {
        сhatBehaviorRepository.setStatusChat(ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP)
    }

    fun openApp() {
        сhatBehaviorRepository.setStatusChat(
            when (сhatBehaviorRepository.getStatusChat()) {
                ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP, ChatStatus.ON_CHAT_SCREEN_BACKGROUND_APP -> ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP
                ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP, ChatStatus.NOT_ON_CHAT_SCREEN_BACKGROUND_APP -> ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP
            }
        )
    }

    fun closeApp() {
        сhatBehaviorRepository.setStatusChat(
            when (сhatBehaviorRepository.getStatusChat()) {
                ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP, ChatStatus.ON_CHAT_SCREEN_BACKGROUND_APP -> ChatStatus.ON_CHAT_SCREEN_BACKGROUND_APP
                ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP, ChatStatus.NOT_ON_CHAT_SCREEN_BACKGROUND_APP -> ChatStatus.NOT_ON_CHAT_SCREEN_BACKGROUND_APP
            }
        )
    }

    fun destroyHostChat() {
        сhatBehaviorRepository.destroyChatSession()
    }

}