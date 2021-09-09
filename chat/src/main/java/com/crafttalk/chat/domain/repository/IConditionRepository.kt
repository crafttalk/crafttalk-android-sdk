package com.crafttalk.chat.domain.repository

import com.crafttalk.chat.initialization.ChatMessageListener
import com.crafttalk.chat.presentation.ChatInternetConnectionListener
import com.crafttalk.chat.utils.ChatStatus

interface IConditionRepository {
    fun setInternetConnectionListener(listener: ChatInternetConnectionListener)
    fun setMessageListener(listener: ChatMessageListener)
    fun setStatusChat(newStatus: ChatStatus)
    fun getStatusChat(): ChatStatus
    fun createSessionChat()
    fun destroySessionChat()
    fun dropChat()

    // проверка вся ли история загружена
    fun getFlagAllHistoryLoaded(): Boolean
    fun saveFlagAllHistoryLoaded(isAllHistoryLoaded: Boolean)
    fun deleteFlagAllHistoryLoaded()

    fun getCurrentReadMessageTime(): Long
    fun saveCurrentReadMessageTime(currentReadMessageTime: Long)
    fun deleteCurrentReadMessageTime()

    // провека наличия сообщения в бд
    suspend fun getStatusExistenceMessages(): Boolean

}