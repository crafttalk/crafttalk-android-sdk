package com.crafttalk.chat.initialization

interface ChatMessageListener {
    fun getNewMessages(countMessages: Int)
}