package com.crafttalk.chat.domain.interactors

import com.crafttalk.chat.domain.repository.IConditionRepository
import com.crafttalk.chat.initialization.ChatMessageListener
import com.crafttalk.chat.presentation.ChatInternetConnectionListener
import com.crafttalk.chat.utils.ChatStatus
import javax.inject.Inject

class ConditionInteractor
@Inject constructor(
    private val conditionRepository: IConditionRepository
) {

    fun setInternetConnectionListener(listener: ChatInternetConnectionListener) {
        conditionRepository.setInternetConnectionListener(listener)
    }

    fun setMessageListener(listener: ChatMessageListener) {
        conditionRepository.setMessageListener(listener)
    }

    fun leaveChatScreen() {
        conditionRepository.setStatusChat(ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP)
    }

    fun goToChatScreen() {
        conditionRepository.setStatusChat(ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP)
    }

    fun openApp() {
        conditionRepository.setStatusChat(
            when (conditionRepository.getStatusChat()) {
                ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP, ChatStatus.ON_CHAT_SCREEN_BACKGROUND_APP -> ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP
                ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP, ChatStatus.NOT_ON_CHAT_SCREEN_BACKGROUND_APP -> ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP
            }
        )
    }

    fun closeApp() {
        conditionRepository.setStatusChat(
            when (conditionRepository.getStatusChat()) {
                ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP, ChatStatus.ON_CHAT_SCREEN_BACKGROUND_APP -> ChatStatus.ON_CHAT_SCREEN_BACKGROUND_APP
                ChatStatus.NOT_ON_CHAT_SCREEN_FOREGROUND_APP, ChatStatus.NOT_ON_CHAT_SCREEN_BACKGROUND_APP -> ChatStatus.NOT_ON_CHAT_SCREEN_BACKGROUND_APP
            }
        )
    }

    fun getStatusChat() = conditionRepository.getStatusChat()

    fun createSessionChat() {
        conditionRepository.createSessionChat()
    }

    fun destroySessionChat() {
        conditionRepository.destroySessionChat()
    }

    fun dropChat() {
        conditionRepository.dropChat()
    }

    fun checkFlagAllHistoryLoaded() =
        conditionRepository.getFlagAllHistoryLoaded()

    fun getCurrentReadMessageTime() =
        conditionRepository.getCurrentReadMessageTime()

    fun getCountUnreadMessages() =
        conditionRepository.getCountUnreadMessages()

    fun getInitialLoadKey(): Int {
        val countUnreadMessages = conditionRepository.getCountUnreadMessages()
        return if (countUnreadMessages > 0) {
            countUnreadMessages - 1
        } else {
            0
        }
    }

    fun saveCurrentReadMessageTime(currentReadMessageTime: Long) {
        conditionRepository.saveCurrentReadMessageTime(currentReadMessageTime)
    }

    fun saveCountUnreadMessages(countUnreadMessages: Int) {
        conditionRepository.saveCountUnreadMessages(countUnreadMessages)
    }

    fun clearDataChatState() {
        conditionRepository.deleteFlagAllHistoryLoaded()
        conditionRepository.deleteCurrentReadMessageTime()
        conditionRepository.deleteAllMessage()
    }

}