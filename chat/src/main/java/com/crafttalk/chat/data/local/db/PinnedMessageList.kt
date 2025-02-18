package com.crafttalk.chat.data.local.db

import com.crafttalk.chat.domain.entity.message.NetworkMessage
import javax.inject.Singleton

@Singleton
object PinnedMessageList {
    private val pinnedList = mutableListOf<NetworkMessage>()

    fun addPinnedMessage(message: NetworkMessage) {
        pinnedList.add(message)
    }
    fun removeAllPinnedMessage(){
        pinnedList.clear()
    }

    fun getPinnedMessage(): List<NetworkMessage> {
        return pinnedList
    }

    fun isEmpty(): Boolean{
        return pinnedList.isEmpty()
    }
}
