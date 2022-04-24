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

    fun getAllMessages(): DataSource.Factory<Int, MessageEntity> =
        messageRepository.getMessages()

    fun getCountUnreadMessages(currentReadMessageTime: Long, timestampLastMessage: Long?): Int? {
        return if (timestampLastMessage == null) {
            messageRepository.getCountUnreadMessages(currentReadMessageTime)
        } else {
            messageRepository.getCountUnreadMessagesRange(currentReadMessageTime, timestampLastMessage)
        }
    }

    fun getCountMessagesInclusiveTimestampById(messageId: String): Int? {
        return messageRepository.getTimestampMessageById(messageId)?.run(messageRepository::getCountMessagesInclusiveTimestamp)
    }

    suspend fun sendMessage(message: String, repliedMessageId: String?) {
        messageRepository.sendMessages(message, repliedMessageId)
    }

    suspend fun selectActionInMessage(messageId: String, actionId: String) {
        messageRepository.selectAction(messageId, actionId)
    }

    suspend fun selectButtonInMessage(messageId: String, actionId: String, buttonId: String) {
        messageRepository.selectButton(messageId, actionId, buttonId)
    }

    suspend fun uploadHistoryMessages(
        eventAllHistoryLoaded: () -> Unit,
        uploadHistoryComplete: () -> Unit,
        executeAnyway: Boolean
    ) {
        val visitor = visitorInteractor.getVisitor() ?: return
        val statusExistenceMessages = conditionRepository.getStatusExistenceMessages()
        val flagAllHistoryLoaded = conditionRepository.getFlagAllHistoryLoaded()

        when {
            !statusExistenceMessages && executeAnyway -> uploadHistoryComplete()
            statusExistenceMessages && (!flagAllHistoryLoaded || executeAnyway) -> messageRepository
                .getTimeFirstMessage()
                ?.let { firstMessageTime ->
                    messageRepository.uploadMessages(
                        uuid = visitor.uuid,
                        startTime = null,
                        endTime = firstMessageTime,
                        updateReadPoint = { false },
                        syncMessagesAcrossDevices = {},
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
        updateReadPoint: (newTimeMark: Long) -> Boolean,
        syncMessagesAcrossDevices: (indexFirstUnreadMessage: Int) -> Unit,
        eventAllHistoryLoaded: () -> Unit
    ) {
        val visitor = visitorInteractor.getVisitor() ?: return
        val syncMessagesAcrossDevicesWrapper: (countUnreadMessages: Int) -> Unit = { countUnreadMessages ->
            syncMessagesAcrossDevices(
                if (countUnreadMessages > 0) countUnreadMessages - 1
                else 0
            )
        }

        if (conditionRepository.getStatusExistenceMessages()) {
            messageRepository.getTimeLastMessage()?.let { lastMessageTime ->
                val messages = messageRepository.uploadMessages(
                    uuid = visitor.uuid,
                    startTime = lastMessageTime + 1,
                    endTime = 0,
                    updateReadPoint = updateReadPoint,
                    syncMessagesAcrossDevices = syncMessagesAcrossDevicesWrapper,
                    returnedEmptyPool = {},
                    getPersonPreview = { personId ->
                        personInteractor.getPersonPreview(personId, visitor.token)
                    },
                    getFileInfo = messageRepository::getFileInfo
                )
                messageRepository.updatePersonNames(messages, personInteractor::updatePersonName)
                messageRepository.mergeNewMessages()
            }
        } else {
//            if (remoteReadMessageTime == 0L) {
                val messages = messageRepository.uploadMessages(
                    uuid = visitor.uuid,
                    startTime = null,
                    endTime = 0,
                    updateReadPoint = updateReadPoint,
                    syncMessagesAcrossDevices = syncMessagesAcrossDevices,
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
                messageRepository.mergeNewMessages()
//            } else {
//                val messages = messageRepository.uploadMessages(
//                    uuid = visitor.uuid,
//                    token = visitor.token,
//                    startTime = remoteReadMessageTime,
//                    endTime = 0,
//                    updateReadPoint = updateReadPoint,
//                    syncMessagesAcrossDevices = syncMessagesAcrossDevices,
//                    returnedEmptyPool = {},
//                    getPersonPreview = { personId ->
//                        personInteractor.getPersonPreview(personId, visitor.token)
//                    },
//                    getFileInfo = messageRepository::getFileInfo
//                )
//                messageRepository.updatePersonNames(messages, personInteractor::updatePersonName)
//                messageRepository.mergeNewMessages()
//            }
        }
    }

    fun updateSizeMessage(id: String, height: Int, width: Int) {
        messageRepository.updateSizeMessage(id, height, width)
    }

    fun removeAllInfoMessages() {
        messageRepository.removeAllInfoMessages()
    }

}