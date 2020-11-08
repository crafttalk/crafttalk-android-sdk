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
        сhatBehaviorRepository.setStatusChat(ChatStatus.NOT_ON_CHAT_SCREEN)
    }

    fun goToChatScreen() {
        сhatBehaviorRepository.setStatusChat(ChatStatus.ON_CHAT_SCREEN)
    }

    fun destroyHostChat() {
        сhatBehaviorRepository.destroyChatSession()
    }

}