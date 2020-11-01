package com.crafttalk.chat.domain.repository

import com.crafttalk.chat.initialization.ChatInternetConnectionListener
import com.crafttalk.chat.initialization.ChatMessageListener

interface IChatBehaviorRepository {
    fun setInternetConnectionListener(listener: ChatInternetConnectionListener)
    fun setMessageListener(listener: ChatMessageListener)
}