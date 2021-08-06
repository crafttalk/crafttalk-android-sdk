package com.crafttalk.chat.domain.interactors

import androidx.paging.DataSource
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.repository.IMessageRepository
import javax.inject.Inject

class ChatMessageInteractor
@Inject constructor(
    private val messageRepository: IMessageRepository,
    private val visitorInteractor: VisitorInteractor
) {
    private var visitorUid: String? = null

    fun getAllMessages(): DataSource.Factory<Int, MessageEntity>? {
        val currentVisitorUid = visitorInteractor.getVisitor()?.uuid
        if (visitorUid == currentVisitorUid) return null
        visitorUid = currentVisitorUid
        return visitorUid?.let { uuid ->
            messageRepository.getMessages(uuid)
        }
    }

    suspend fun sendMessage(message: String) {
        messageRepository.sendMessages(message)
    }

    suspend fun selectActionInMessage(messageId: String, actionId: String) {
        val uuid = visitorInteractor.getVisitor()?.uuid ?: return
        messageRepository.selectAction(uuid, messageId, actionId)
    }

    fun syncMessages(isEmptyDB: Boolean) {
        if (isEmptyDB) {
            messageRepository.syncMessages(0)
        } else {
            val uuid = visitorInteractor.getVisitor()?.uuid ?: return
            val firstMessageTime = messageRepository.getFirstMessageTime(uuid)

            firstMessageTime?.let { time ->
                messageRepository.syncMessages(time)
            }
        }
    }

    fun updateSizeMessage(idKey: Long, height: Int, width: Int) {
        messageRepository.updateSizeMessage(idKey, height, width)
    }

    fun readMessage(id: String) {
        val currentVisitorUid = visitorInteractor.getVisitor()?.uuid ?: return
        messageRepository.readMessage(currentVisitorUid, id)
    }

}