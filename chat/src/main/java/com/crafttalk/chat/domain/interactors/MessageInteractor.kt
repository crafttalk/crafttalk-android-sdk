package com.crafttalk.chat.domain.interactors

import android.util.Log
import androidx.paging.DataSource
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.message.MessageType
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

    fun clearDbIfMessagesDuplicated() {
        val messages = messageRepository.getAllMessages()
        if (messages.size != messages.distinctBy { it.id }.size) {
            messageRepository.removeAllMessages()
        }
    }

    fun getCountUnreadMessages(currentReadMessageTime: Long, timestampLastMessage: Long?): Int? {
        return if (timestampLastMessage == null) {
            messageRepository.getCountUnreadMessages(
                currentReadMessageTime = currentReadMessageTime,
                ignoredMessageTypes = listOf(MessageType.INFO_MESSAGE.valueType)
            )
        } else {
            messageRepository.getCountUnreadMessagesRange(
                currentReadMessageTime = currentReadMessageTime,
                timestampLastMessage = timestampLastMessage,
                ignoredMessageTypes = listOf(MessageType.INFO_MESSAGE.valueType)
            )
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

    fun selectButtonInWidget(actionId: String) {
        messageRepository.selectButtonInWidget(actionId)
    }

    suspend fun uploadHistoryMessages(
        eventAllHistoryLoaded: () -> Unit,
        uploadHistoryComplete: () -> Unit,
        updateSearchMessagePosition: suspend (insertedMessages: List<MessageEntity>) -> Unit,
        executeAnyway: Boolean
    ) {
        val visitor = visitorInteractor.getVisitor() ?: return
        val statusExistenceMessages = conditionRepository.getStatusExistenceMessages()
        val flagAllHistoryLoaded = conditionRepository.getFlagAllHistoryLoaded()

        Log.d("TEST_LOG_HISTORY", "uploadHistoryMessages interactor statusExistenceMessages: ${statusExistenceMessages}; flagAllHistoryLoaded: ${flagAllHistoryLoaded};")
        when {
            !statusExistenceMessages && executeAnyway -> {
                Log.d("TEST_LOG_HISTORY", "uploadHistoryMessages interactor 1;")
                uploadHistoryComplete()
            }
            statusExistenceMessages && (!flagAllHistoryLoaded || executeAnyway) -> {
                Log.d("TEST_LOG_HISTORY", "uploadHistoryMessages interactor 2;")
                messageRepository
                    .getTimeFirstMessage()
                    ?.let { firstMessageTime ->
                        messageRepository.uploadMessages(
                            uuid = visitor.uuid,
                            startTime = null,
                            endTime = firstMessageTime,
                            updateReadPoint = { false },
                            syncMessagesAcrossDevices = {},
                            returnedEmptyPool = {
                                Log.d("TEST_LOG_HISTORY", "returnedEmptyPool 1;")
                                eventAllHistoryLoaded()
                                conditionRepository.saveFlagAllHistoryLoaded(true)
                            },
                            getPersonPreview = { personId ->
                                personInteractor.getPersonPreview(personId, visitor.token)
                            },
                            getFileInfo = messageRepository::getFileInfo,
                            updateSearchMessagePosition = updateSearchMessagePosition
                        )
                        uploadHistoryComplete()
                    }
            }
            else -> {
                Log.d("TEST_LOG_HISTORY", "uploadHistoryMessages interactor 3;")
            }
        }
    }

    // при переходе на холд добавить вызов метода, обновляющего состояния у сообщений, находящихся в статусе "отправляется"
    suspend fun syncMessages(
        updateReadPoint: (newTimeMark: Long) -> Boolean,
        syncMessagesAcrossDevices: (indexFirstUnreadMessage: Int) -> Unit,
        eventAllHistoryLoaded: () -> Unit,
        updateSearchMessagePosition: suspend (insertedMessages: List<MessageEntity>) -> Unit
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
                    returnedEmptyPool = {
                        Log.d("TEST_LOG_HISTORY", "returnedEmptyPool 2;")
                    },
                    getPersonPreview = { personId ->
                        personInteractor.getPersonPreview(personId, visitor.token)
                    },
                    getFileInfo = messageRepository::getFileInfo,
                    updateSearchMessagePosition = updateSearchMessagePosition
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
                        Log.d("TEST_LOG_HISTORY", "returnedEmptyPool 3;")
                        eventAllHistoryLoaded()
                        conditionRepository.saveFlagAllHistoryLoaded(true)
                    },
                    getPersonPreview = { personId ->
                        personInteractor.getPersonPreview(personId, visitor.token)
                    },
                    getFileInfo = messageRepository::getFileInfo,
                    updateSearchMessagePosition = updateSearchMessagePosition
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

    fun setUpdateSearchMessagePosition(updateSearchMessagePosition: suspend (insertedMessages: List<MessageEntity>) -> Unit) {
        messageRepository.setUpdateSearchMessagePosition(updateSearchMessagePosition)
    }
}