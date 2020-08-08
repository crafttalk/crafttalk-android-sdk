package com.crafttalk.chat.domain.repository

import com.crafttalk.chat.domain.entity.message.Message

interface IDataRepository {
    suspend fun insert(messageSocket: Message)
    suspend fun marge(arrayMessages: Array<Message>)
}