package com.crafttalk.chat.domain.repository

import androidx.paging.DataSource
import com.crafttalk.chat.data.local.db.entity.MessageEntity

interface IMessageRepository {
    fun getMessages(uuid: String): DataSource.Factory<Int, MessageEntity>
    fun getMessagesList(): List<MessageEntity>
    suspend fun sendMessages(message: String)
    fun syncMessages(timestamp: Long)
    suspend fun selectAction(uuid: String, messageId: String, actionId: String)

    fun getFirstMessageTime(uuid: String): Long?

    fun updateSizeMessage(idKey: Long, height: Int, width: Int)
    fun readMessage(uuid: String, id: String)
}