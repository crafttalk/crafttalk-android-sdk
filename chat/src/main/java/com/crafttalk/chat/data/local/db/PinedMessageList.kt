package com.crafttalk.chat.data.local.db

import com.crafttalk.chat.domain.entity.message.NetworkMessage
import javax.inject.Singleton

@Singleton
object PinedMessageList {
    private val pinedList = mutableListOf<NetworkMessage>()

    fun addPinedMessage(message: NetworkMessage) {
        pinedList.add(message)
    }
    fun removeAllPinedMessage(){
        pinedList.clear()
    }

    fun getPinedMessage(): List<NetworkMessage> {
        return pinedList
    }

    fun isEmpty(): Boolean{
        return pinedList.isEmpty()
    }
}
