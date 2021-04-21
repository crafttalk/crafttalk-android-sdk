package com.crafttalk.chat.data.repository

import androidx.paging.DataSource
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.entity.ActionEntity
import com.crafttalk.chat.domain.repository.IMessageRepository
import javax.inject.Inject
import com.crafttalk.chat.data.local.db.entity.MessageEntity

class MessageRepository
@Inject constructor(
    private val dao: MessagesDao,
    private val socketApi: SocketApi
) : IMessageRepository {

    override fun getMessages(uuid: String): DataSource.Factory<Int, MessageEntity> {
        return dao.getMessages(uuid)
    }

    override suspend fun sendMessages(message: String) {
        socketApi.sendMessage(message)
    }

    override fun syncMessages(timestamp: Long) {
        socketApi.sync(timestamp)
    }

    override suspend fun selectAction(uuid: String, messageId: String, actionId: String) {
        socketApi.selectAction(actionId)
        dao.getMessageById(uuid, messageId)?.let {
            val updatedActions = it.actions?.map { action ->
                ActionEntity(
                    action.actionId,
                    action.actionText,
                    action.actionId == actionId
                )
            }
            dao.selectAction(uuid, messageId, updatedActions)
        }
    }

    override fun getFirstMessageTime(uuid: String): Long? {
        return dao.getFirstMessageTime(uuid)
    }

    override fun updateSizeMessage(idKey: Long, height: Int, width: Int) {
        dao.updateSizeMessage(idKey, height, width)
    }

    override fun readMessage(uuid: String, id: String) {
        dao.readMessage(uuid, id)
    }

}