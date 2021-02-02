package com.crafttalk.chat.domain.repository

import androidx.paging.DataSource
import com.crafttalk.chat.data.local.db.entity.Message as MessageDB

interface IMessageRepository {
    fun getMessages(uuid: String): DataSource.Factory<Int, MessageDB>
    suspend fun sendMessages(message: String)
    suspend fun syncMessages(timestamp: Long)
    suspend fun selectAction(actionId: String)

    fun updateSizeMessage(idKey: Long, height: Int, width: Int)
}