package com.crafttalk.chat.data.repository

import android.util.Log
import com.crafttalk.chat.data.helper.converters.text.convertFromHtmlToNormalString
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.entity.Message as MessageDB
import com.crafttalk.chat.domain.entity.message.Message as MessageSocket
import com.crafttalk.chat.domain.entity.message.MessageType
import com.crafttalk.chat.domain.entity.tags.Tag
import com.crafttalk.chat.domain.repository.IDataRepository
import javax.inject.Inject

class DataRepository
@Inject constructor(
    private val dao: MessagesDao
): IDataRepository {

    override suspend fun insert(messageSocket: MessageSocket) {
        when(messageSocket.messageType) {
            MessageType.VISITOR_MESSAGE.valueType -> {
                Log.d("REPOSITORY", "insertMessage $messageSocket")
                dao.insertMessage(MessageDB.map(messageSocket))
            }
            MessageType.RECEIVED_BY_MEDIATO.valueType, MessageType.RECEIVED_BY_OPERATOR.valueType -> {
                Log.d("REPOSITORY", "updateMessage: messageType: ${messageSocket.messageType}")
                messageSocket.parentMessageId?.let {
                    dao.updateMessage(it, messageSocket.messageType)
                }
            }
        }
    }

    override suspend fun marge(arrayMessages: Array<MessageSocket>) {
        val messagesFromDb = dao.getMessagesList()
        arrayMessages.sortWith(compareBy(MessageSocket::timestamp))
        arrayMessages.forEach { messageFromHistory ->
            val list = arrayListOf<Tag>()
            val message = messageFromHistory.message?.convertFromHtmlToNormalString(list)

            val messageCheckObj = MessageDB(
                id = messageFromHistory.id,
                messageType = messageFromHistory.messageType,
                isReply = messageFromHistory.isReply,
                parentMsgId = messageFromHistory.parentMessageId,
                timestamp = messageFromHistory.timestamp,
                message = message,
                spanStructureList = list,
                actions = messageFromHistory.actions,
                attachmentUrl = messageFromHistory.attachmentUrl,
                attachmentType = messageFromHistory.attachmentType,
                attachmentName = messageFromHistory.attachmentName,
                operatorName = if (messageFromHistory.operatorName == null || !messageFromHistory.isReply) "Вы" else messageFromHistory.operatorName,
                height = 0,
                width = 0
            )
            when (messageCheckObj.messageType) {
                MessageType.VISITOR_MESSAGE.valueType -> {
                    if (messageCheckObj.isReply) {
                        // serv
                        if (!messagesFromDb.any { it.id == messageCheckObj.id }) {
                            dao.insertMessage(messageCheckObj)
                        }
                    }
                    else {
                        // user
                        if (messageCheckObj !in messagesFromDb) {
                            Log.d("REPOSITORY", "insert message $messageCheckObj")
                            dao.insertMessage(messageCheckObj)
                        }
                    }
                }
                MessageType.RECEIVED_BY_MEDIATO.valueType, MessageType.RECEIVED_BY_OPERATOR.valueType -> {
                    Log.d("REPOSITORY", "update message id - ${messageCheckObj.parentMsgId}, type - ${messageCheckObj.messageType}")
                    messageCheckObj.parentMsgId?.let { parentId ->
                        dao.updateMessage(parentId, messageCheckObj.messageType)
                    }
                }
            }

        }
    }

}