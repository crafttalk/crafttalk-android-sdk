package com.crafttalk.chat.domain.interactors

import androidx.lifecycle.LiveData
import com.crafttalk.chat.data.local.db.entity.Message
import com.crafttalk.chat.domain.repository.IMessageRepository
import javax.inject.Inject

class ChatMessageInteractor
@Inject constructor(
    private val messageRepository: IMessageRepository
) {

    fun getAllMessages(): LiveData<List<Message>> = messageRepository.getMessagesList()

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

}