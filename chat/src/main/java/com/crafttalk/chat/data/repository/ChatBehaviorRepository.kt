package com.crafttalk.chat.data.repository

import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.domain.repository.IChatBehaviorRepository
import com.crafttalk.chat.initialization.ChatInternetConnectionListener
import com.crafttalk.chat.initialization.ChatMessageListener
import javax.inject.Inject

class ChatBehaviorRepository
@Inject constructor(
    private val socketApi: SocketApi
) : IChatBehaviorRepository {

    override fun setInternetConnectionListener(listener: ChatInternetConnectionListener) {
        socketApi.setInternetConnectionListener(listener)
    }

    override fun setMessageListener(listener: ChatMessageListener) {
        socketApi.setMessageListener(listener)
    }

}