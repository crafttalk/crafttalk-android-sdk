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

    suspend fun sendMessage(message: String, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        try {
            messageRepository.sendMessages(message)
            success()
        }
        catch (ex: Throwable) {
            fail(ex)
        }
    }

    suspend fun selectActionInMessage(messageId: String, actionId: String, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        try {
            val uuid = visitorInteractor.getVisitor()?.uuid ?: return
            messageRepository.selectAction(uuid, messageId, actionId)
            success()
        }
        catch (ex: Throwable) {
            fail(ex)
        }
    }

    fun syncMessages(isEmptyDB: Boolean) {
        if (isEmptyDB) {
            messageRepository.syncMessages(System.currentTimeMillis())
        } else {
            val uuid = visitorInteractor.getVisitor()?.uuid ?: return
            val firstMessageTime = messageRepository.getFirstMessageTime(uuid)

            firstMessageTime?.let { time ->
                messageRepository.syncMessages(time)
            }
        }
    }

    fun updateSizeMessage(idKey: Long, height: Int, width: Int, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        try {
            messageRepository.updateSizeMessage(idKey, height, width)
            success()
        }
        catch (ex: Throwable) {
            fail(ex)
        }
    }

    fun readMessage(id: String) {
        val currentVisitorUid = visitorInteractor.getVisitor()?.uuid ?: return
        messageRepository.readMessage(currentVisitorUid, id)
    }

}