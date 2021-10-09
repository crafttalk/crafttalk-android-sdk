package com.crafttalk.chat.data.repository

import android.content.Context
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.entity.ActionEntity
import com.crafttalk.chat.domain.repository.IMessageRepository
import javax.inject.Inject
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.file.TypeDownloadProgress
import com.crafttalk.chat.domain.entity.message.MessageType
import com.crafttalk.chat.domain.entity.message.NetworkMessage
import com.crafttalk.chat.domain.transfer.TransferFileInfo
import com.crafttalk.chat.presentation.helper.ui.getSizeMediaFile
import com.crafttalk.chat.presentation.helper.ui.getWeightFile
import com.crafttalk.chat.utils.ChatParams
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull

class MessageRepository
@Inject constructor(
    private val context: Context,
    private val messagesDao: MessagesDao,
    private val socketApi: SocketApi
) : IMessageRepository {

    override fun getMessages() = messagesDao
        .getMessages()

    override fun getCountUnreadMessages(currentReadMessageTime: Long) = messagesDao
        .getCountUnreadMessages(currentReadMessageTime)

    override fun getCountUnreadMessagesRange(
        currentReadMessageTime: Long,
        timestampLastMessage: Long
    ) = messagesDao
        .getCountUnreadMessagesRange(currentReadMessageTime, timestampLastMessage)

    override suspend fun getTimeFirstMessage() = messagesDao
        .getFirstTime()

    override suspend fun getTimeLastMessage() = messagesDao
        .getLastTime()

    override suspend fun uploadMessages(
        uuid: String,
        startTime: Long?,
        endTime: Long,
        updateReadPoint: (newPosition: Long) -> Boolean,
        syncMessagesAcrossDevices: (countUnreadMessages: Int) -> Unit,
        allMessageLoaded: () -> Unit,
        getPersonPreview: suspend (personId: String) -> String?,
        getFileInfo: suspend (context: Context, networkMessage: NetworkMessage) -> TransferFileInfo?
    ): List<MessageEntity> {

        try {
            val fullPullMessages= mutableListOf<NetworkMessage>()

            var lastTimestamp = endTime
            while (true) {
                val listMessages = withTimeoutOrNull(ChatParams.uploadPoolMessagesTimeout) {
                    async {
                        socketApi.uploadMessages(
                            timestamp = lastTimestamp
                        )
                    }.await()
                }
                socketApi.closeHistoryListener()
                listMessages ?: break

                if (startTime == null) {
                    fullPullMessages.addAll(listMessages)
//                    Раскоментить когда исправиться задача ...
//                    val countRealMessages = listMessages.filter {
//                        it.messageType == MessageType.VISITOR_MESSAGE.valueType &&
//                        it.isContainsContent &&
//                        it.selectedAction.isNullOrBlank()
//                    }.size
                    if (listMessages.isEmpty() /*|| countRealMessages < ChatParams.countDownloadedMessages*/) {
                        allMessageLoaded()
                    }
                    break
                }

                val firstTimeMessage = listMessages
                    .sortedBy { it.timestamp }
                    .find { it.messageType in listOf(MessageType.VISITOR_MESSAGE.valueType, MessageType.TRANSFER_TO_OPERATOR.valueType) }?.timestamp

                fullPullMessages.addAll(listMessages.filter { it.timestamp >= startTime })

                if (firstTimeMessage == null) {
                    allMessageLoaded()
                    break
                }
                if (firstTimeMessage <= startTime) break

                lastTimestamp = firstTimeMessage
            }

            if (fullPullMessages.isEmpty()) return listOf()

            val actionSelectionMessages = fullPullMessages.filter { !it.selectedAction.isNullOrBlank() && it.messageType == MessageType.VISITOR_MESSAGE.valueType }.map { it.selectedAction ?: "" }
            val messageStatuses = fullPullMessages.filter { it.messageType in listOf(MessageType.RECEIVED_BY_MEDIATO.valueType, MessageType.RECEIVED_BY_OPERATOR.valueType) }

            val operatorMessagesWithContent = fullPullMessages.filter { it.isReply && it.messageType == MessageType.VISITOR_MESSAGE.valueType && it.isContainsContent }.map { networkMessage ->
                val fileInfo = getFileInfo(context, networkMessage)
                MessageEntity.mapOperatorMessage(
                    uuid = uuid,
                    networkMessage = networkMessage,
                    actionsSelected = actionSelectionMessages,
                    operatorPreview = networkMessage.operatorId?.let { getPersonPreview(it) },
                    fileSize = fileInfo?.size,
                    mediaFileHeight = fileInfo?.height,
                    mediaFileWidth = fileInfo?.width
                )
            }

            val userMessagesWithContent = fullPullMessages.filter { !it.isReply &&  it.messageType == MessageType.VISITOR_MESSAGE.valueType && it.isContainsContent }.map { networkMessage ->
                val statusesConcreteMessage: List<Int> = messageStatuses.filter { it.parentMessageId == networkMessage.idFromChannel }.map { it.messageType }
                val newStatus: Int = when {
                    statusesConcreteMessage.contains(MessageType.RECEIVED_BY_OPERATOR.valueType) -> MessageType.RECEIVED_BY_OPERATOR.valueType
                    statusesConcreteMessage.contains(MessageType.RECEIVED_BY_MEDIATO.valueType) -> MessageType.RECEIVED_BY_MEDIATO.valueType
                    else -> MessageType.VISITOR_MESSAGE.valueType
                }
                val fileInfo = getFileInfo(context, networkMessage)
                val repliedFileInfo = networkMessage.replyToMessage?.let { getFileInfo(context, it) }
                MessageEntity.mapUserMessage(
                    uuid = uuid,
                    networkMessage = networkMessage,
                    status = newStatus,
                    operatorPreview = networkMessage.operatorId?.let { getPersonPreview(it) },
                    fileSize = fileInfo?.size,
                    mediaFileHeight = fileInfo?.height,
                    mediaFileWidth = fileInfo?.width,
                    repliedMessageFileSize = repliedFileInfo?.size,
                    repliedMessageMediaFileHeight = repliedFileInfo?.height,
                    repliedMessageMediaFileWidth = repliedFileInfo?.width,
                )
            }

            val messagesAboutJoin = fullPullMessages.filter { it.messageType == MessageType.TRANSFER_TO_OPERATOR.valueType }.map { networkMessage ->
                MessageEntity.mapOperatorJoinMessage(
                    uuid = uuid,
                    networkMessage = networkMessage,
                    operatorPreview = networkMessage.operatorId?.let { getPersonPreview(it) }
                )
            }

            val maxTimestampUserMessage = userMessagesWithContent.maxByOrNull { it.timestamp }?.timestamp
            maxTimestampUserMessage?.run(updateReadPoint)

            val resultMessages = mutableListOf<MessageEntity>().apply {
                addAll(operatorMessagesWithContent)
                addAll(userMessagesWithContent)
                addAll(messagesAboutJoin)
            }

            messagesDao.insertMessages(resultMessages)

            maxTimestampUserMessage?.let { timestampLastUserMessage ->
                resultMessages.filter { it.timestamp > timestampLastUserMessage }.size.run(syncMessagesAcrossDevices)
            }

            return resultMessages
        } catch (ex: Exception) {
            return listOf()
        }
    }

    override suspend fun mergeNewMessages() {
        socketApi.mergeNewMessages()
    }

    override suspend fun updatePersonNames(
        messages: List<MessageEntity>,
        updatePersonName: suspend (personId: String?, currentPersonName: String?) -> Unit
    ) {
        messages.sortedBy { it.timestamp }.forEach {
            updatePersonName(it.operatorId, it.operatorName)
        }
    }

    override suspend fun getFileInfo(
        context: Context,
        networkMessage: NetworkMessage
    ): TransferFileInfo? {
        return when {
            (MessageType.VISITOR_MESSAGE.valueType == networkMessage.messageType) && (networkMessage.isImage || networkMessage.isGif) -> {
                networkMessage.attachmentUrl?.let { url ->
                    val pair = getSizeMediaFile(context, url)
                    TransferFileInfo(
                        height = pair?.first,
                        width = pair?.second
                    )
                }
            }
            (MessageType.VISITOR_MESSAGE.valueType == networkMessage.messageType) && networkMessage.isFile -> {
                networkMessage.attachmentUrl?.let { url ->
                    TransferFileInfo(
                        size = getWeightFile(url)
                    )
                }
            }
            else -> null
        }
    }

    override suspend fun sendMessages(message: String, repliedMessageId: String?) {
        val repliedMessage = repliedMessageId?.let { messagesDao.getMessageById(it) }?.let { NetworkMessage.map(it) }
        socketApi.sendMessage(message, repliedMessage)
    }

    override suspend fun selectAction(messageId: String, actionId: String) {
        socketApi.selectAction(actionId)
        messagesDao.getMessageById(messageId)?.let {
            val updatedActions = it.actions?.map { action ->
                ActionEntity(
                    action.actionId,
                    action.actionText,
                    action.actionId == actionId
                )
            }
            messagesDao.selectAction(messageId, updatedActions)
        }
    }

    override fun updateSizeMessage(id: String, height: Int, width: Int) {
        messagesDao.updateSizeMessage(id, height, width)
    }

    override fun updateTypeDownloadProgressOfMessageWithAttachment(
        id: String,
        typeDownloadProgress: TypeDownloadProgress
    ) {
        messagesDao.updateTypeDownloadProgress(id, typeDownloadProgress)
    }

}