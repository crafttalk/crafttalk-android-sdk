package com.crafttalk.chat.domain.interactors

import androidx.paging.DataSource
import com.crafttalk.chat.data.local.db.entity.Message
import com.crafttalk.chat.domain.repository.IMessageRepository
import javax.inject.Inject

class ChatMessageInteractor
@Inject constructor(
    private val messageRepository: IMessageRepository,
    private val visitorInteractor: VisitorInteractor
) {
    private var visitorUid: String? = null

    fun getAllMessages(): DataSource.Factory<Int, Message>? {
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

    suspend fun selectActionInMessage(actionId: String, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        try {
            messageRepository.selectAction(actionId)
            success()
        }
        catch (ex: Throwable) {
            fail(ex)
        }
    }

    suspend fun syncMessages(timestamp: Long = 0, success: () -> Unit, fail: (ex: Throwable) -> Unit) {
        try {
            messageRepository.syncMessages(timestamp)
            success()
        }
        catch (ex: Throwable) {
            fail(ex)
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