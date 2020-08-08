package com.crafttalk.chat.data.repository

import androidx.lifecycle.LiveData
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.remote.socket_service.SocketApi
import com.crafttalk.chat.domain.repository.IMessageRepository
import com.crafttalk.chat.data.local.db.entity.Message as MessageDB

class MessageRepository constructor(
    private val dao: MessagesDao,
    private val socketApi: SocketApi
) : IMessageRepository {

    override fun getMessagesList(): LiveData<List<MessageDB>> {
        return dao.getMessagesLiveData()
    }

    override suspend fun sendMessages(message: String) {
        socketApi.sendMessage(message)
    }

    override suspend fun syncMessages(timestamp: Long) {
        socketApi.sync(timestamp)
    }

    override suspend fun selectAction(actionId: String) {
        socketApi.selectAction(actionId)
    }

    override fun updateSizeMessage(idKey: Long, height: Int, width: Int) {
        dao.updateSizeMessage(idKey, height, width)
    }

}