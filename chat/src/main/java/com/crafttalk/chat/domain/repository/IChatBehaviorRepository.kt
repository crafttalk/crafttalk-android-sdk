package com.crafttalk.chat.domain.repository

import com.crafttalk.chat.initialization.ChatMessageListener
import com.crafttalk.chat.presentation.ChatInternetConnectionListener
import com.crafttalk.chat.utils.ChatStatus

interface IChatBehaviorRepository {
    fun setInternetConnectionListener(listener: ChatInternetConnectionListener)
    fun setMessageListener(listener: ChatMessageListener)
    fun setStatusChat(newStatus: ChatStatus)
    fun getStatusChat(): ChatStatus
    fun destroyChatSession()

    fun giveFeedbackOnOperator(countStars: Int)
}