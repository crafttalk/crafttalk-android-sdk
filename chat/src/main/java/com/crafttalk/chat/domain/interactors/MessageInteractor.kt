package com.crafttalk.chat.domain.interactors

import androidx.paging.DataSource
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.repository.IConditionRepository
import com.crafttalk.chat.domain.repository.IMessageRepository
import javax.inject.Inject

class MessageInteractor
@Inject constructor(
    private val messageRepository: IMessageRepository,
    private val conditionRepository: IConditionRepository,
    private val visitorInteractor: VisitorInteractor,
    private val personInteractor: PersonInteractor
) {
    private var visitorUid: String? = null

    fun getAllMessages(): DataSource.Factory<Int, MessageEntity>? {
        val currentVisitorUid = visitorInteractor.getVisitor()?.uuid
        if (visitorUid == currentVisitorUid) return null
        visitorUid = currentVisitorUid
        return visitorUid?.run(messageRepository::getMessages)
    }

    suspend fun sendMessage(message: String) {
        messageRepository.sendMessages(message)
    }

    suspend fun selectActionInMessage(messageId: String, actionId: String) {
        val uuid = visitorInteractor.getVisitor()?.uuid ?: return
        messageRepository.selectAction(uuid, messageId, actionId)
    }

    suspend fun uploadHistoryMessages(
        eventAllHistoryLoaded: () -> Unit,
        uploadHistoryComplete: () -> Unit
    ) {
        val visitor = visitorInteractor.getVisitor() ?: return
        if (conditionRepository.getStatusExistenceMessages(visitor.uuid) && !conditionRepository.getFlagAllHistoryLoaded()) {
            messageRepository.getTimeFirstMessage(visitor.uuid)?.let { firstMessageTime ->
                messageRepository.uploadMessages(
                    uuid = visitor.uuid,
                    token = visitor.token,
                    startTime = null,
                    endTime = firstMessageTime,
                    updateReadPoint = {},
                    returnedEmptyPool = {
                        eventAllHistoryLoaded()
                        conditionRepository.saveFlagAllHistoryLoaded(true)
                    },
                    getPersonPreview = { personId ->
                        personInteractor.getPersonPreview(personId, visitor.token)
                    },
                    getFileInfo = messageRepository::getFileInfo
                )
                uploadHistoryComplete()
            }
        }
    }

    // при переходе на холд добавить вызов метода, обновляющего состояния у сообщений, находящихся в статусе "отправляется"
    suspend fun syncMessages(
        currentReadMessageTime: Long,
        updateReadPoint: (newTimeMark: Long) -> Unit,
        eventAllHistoryLoaded: () -> Unit,
        syncComplete: () -> Unit
    ) {
        val visitor = visitorInteractor.getVisitor() ?: return
        if (conditionRepository.getStatusExistenceMessages(visitor.uuid)) {
            messageRepository.getTimeLastMessage(visitor.uuid)?.let { lastMessageTime ->
                val messages = messageRepository.uploadMessages(
                    uuid = visitor.uuid,
                    token = visitor.token,
                    startTime = lastMessageTime + 1,
                    endTime = 0,
                    updateReadPoint = updateReadPoint,
                    returnedEmptyPool = {},
                    getPersonPreview = { personId ->
                        personInteractor.getPersonPreview(personId, visitor.token)
                    },
                    getFileInfo = messageRepository::getFileInfo
                )
                messageRepository.updatePersonNames(messages, personInteractor::updatePersonName)
                syncComplete()
            }
        } else {
            if (currentReadMessageTime == 0L) {
                val messages = messageRepository.uploadMessages(
                    uuid = visitor.uuid,
                    token = visitor.token,
                    startTime = null,
                    endTime = 0,
                    updateReadPoint = updateReadPoint,
                    returnedEmptyPool = {
                        eventAllHistoryLoaded()
                        conditionRepository.saveFlagAllHistoryLoaded(true)
                    },
                    getPersonPreview = { personId ->
                        personInteractor.getPersonPreview(personId, visitor.token)
                    },
                    getFileInfo = messageRepository::getFileInfo
                )
                messageRepository.updatePersonNames(messages, personInteractor::updatePersonName)
                syncComplete()
            } else {
                val messages = messageRepository.uploadMessages(
                    uuid = visitor.uuid,
                    token = visitor.token,
                    startTime = currentReadMessageTime,
                    endTime = 0,
                    updateReadPoint = updateReadPoint,
                    returnedEmptyPool = {},
                    getPersonPreview = { personId ->
                        personInteractor.getPersonPreview(personId, visitor.token)
                    },
                    getFileInfo = messageRepository::getFileInfo
                )
                messageRepository.updatePersonNames(messages, personInteractor::updatePersonName)
                syncComplete()
            }
        }
    }

    fun updateSizeMessage(id: String, height: Int, width: Int) {
        val currentVisitorUid = visitorInteractor.getVisitor()?.uuid ?: return
        messageRepository.updateSizeMessage(currentVisitorUid, id, height, width)
    }

}