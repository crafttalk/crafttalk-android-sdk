package com.crafttalk.chat.data.repository

import android.content.Context
import com.crafttalk.chat.data.api.rest.MessageApi
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.data.helper.network.toData
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.entity.ActionEntity
import com.crafttalk.chat.data.local.db.entity.ButtonEntity
import com.crafttalk.chat.data.local.db.entity.KeyboardEntity
import com.crafttalk.chat.domain.repository.IMessageRepository
import javax.inject.Inject
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.file.TypeDownloadProgress
import com.crafttalk.chat.domain.entity.message.MessageType
import com.crafttalk.chat.domain.entity.message.NetworkBodySearch
import com.crafttalk.chat.domain.entity.message.NetworkMessage
import com.crafttalk.chat.domain.entity.message.NetworkSearch
import com.crafttalk.chat.domain.transfer.TransferFileInfo
import com.crafttalk.chat.presentation.helper.ui.getSizeMediaFile
import com.crafttalk.chat.presentation.helper.ui.getWeightFile
import com.crafttalk.chat.presentation.helper.ui.getWeightMediaFile
import com.crafttalk.chat.utils.ChatParams
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull

class MessageRepository
@Inject constructor(
    private val context: Context,
    private val messagesDao: MessagesDao,
    private val socketApi: SocketApi,
    private val messageApi: MessageApi
) : IMessageRepository {

    override fun getMessages() = messagesDao
        .getMessages()

    override fun getAllMessages() = messagesDao
        .getAllMessages()

    override fun getCountUnreadMessages(
        currentReadMessageTime: Long,
        ignoredMessageTypes: List<Int>
    ) = messagesDao
        .getCountUnreadMessages(
            currentReadMessageTime= currentReadMessageTime,
            ignoredMessageTypes = ignoredMessageTypes
        )

    override suspend fun getPositionByTimestamp(id: String, timestamp: Long): Int? {
        return if (messagesDao.emptyAvailable(id)) {
            messagesDao.getPositionByTimestamp(timestamp)
        } else {
            null
        }
    }

    override fun getTimestampMessageById(messageId: String) = messagesDao
        .getTimestampMessageById(messageId)

    override fun getCountMessagesInclusiveTimestamp(timestampMessage: Long) = messagesDao
        .getCountMessagesInclusiveTimestamp(timestampMessage)

    override fun getCountUnreadMessagesRange(
        currentReadMessageTime: Long,
        timestampLastMessage: Long,
        ignoredMessageTypes: List<Int>
    ) = messagesDao
        .getCountUnreadMessagesRange(
            currentReadMessageTime = currentReadMessageTime,
            timestampLastMessage = timestampLastMessage,
            ignoredMessageTypes = ignoredMessageTypes
        )

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
        getFileInfo: suspend (context: Context, networkMessage: NetworkMessage) -> TransferFileInfo?,
        updateSearchMessagePosition: suspend (insertedMessages: List<MessageEntity>) -> Unit
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
                    arrivalTime = System.currentTimeMillis(),
                    actionsSelected = actionSelectionMessages,
                    buttonsSelected = listOf(),
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
                    arrivalTime = System.currentTimeMillis(),
                    status = newStatus,
                    operatorPreview = networkMessage.operatorId?.let { getPersonPreview(it) },
                    fileSize = fileInfo?.size,
                    mediaFileHeight = fileInfo?.height,
                    mediaFileWidth = fileInfo?.width,
                    repliedMessageFileSize = repliedFileInfo?.size,
                    repliedMessageMediaFileHeight = repliedFileInfo?.height,
                    repliedMessageMediaFileWidth = repliedFileInfo?.width
                )
            }

            val messagesAboutJoin = fullPullMessages.filter { it.messageType == MessageType.TRANSFER_TO_OPERATOR.valueType }.map { networkMessage ->
                MessageEntity.mapOperatorJoinMessage(
                    uuid = uuid,
                    networkMessage = networkMessage,
                    arrivalTime = System.currentTimeMillis(),
                    operatorPreview = networkMessage.operatorId?.let { getPersonPreview(it) }
                )
            }

            val maxTimestampUserMessage = userMessagesWithContent.maxBy { it.timestamp }?.timestamp
            maxTimestampUserMessage?.run(updateReadPoint)

            val resultMessages = mutableListOf<MessageEntity>().apply {
                addAll(operatorMessagesWithContent.distinctBy { it.id }.filter { !messagesDao.hasThisMessage(it.id) })
                addAll(userMessagesWithContent.distinctBy { it.id }.filter { !messagesDao.hasThisMessage(it.id) })
                addAll(messagesAboutJoin.distinctBy { it.id }.filter { !messagesDao.hasThisMessage(it.id) })
            }

            ChatParams.glueMessage?.let { msg ->
                resultMessages.add(MessageEntity.mapInfoMessage(
                    uuid = uuid,
                    infoMessage = msg,
                    timestamp = ((resultMessages.maxBy { it.timestamp })?.timestamp ?: messagesDao.getLastTime() ?: System.currentTimeMillis()) + 1,
                    arrivalTime = System.currentTimeMillis()
                ))
            }

            removeAllInfoMessages()
            updateSearchMessagePosition(resultMessages)
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
            (networkMessage.messageType in listOf(MessageType.VISITOR_MESSAGE.valueType, MessageType.RECEIVED_BY_MEDIATO.valueType, MessageType.RECEIVED_BY_OPERATOR.valueType)) && (networkMessage.isImage || networkMessage.isGif) -> {
                networkMessage.attachmentUrl?.let { url ->
                    val pair = getSizeMediaFile(context, url)
                    TransferFileInfo(
                        height = pair?.first,
                        width = pair?.second
                    )
                }
            }
            (networkMessage.messageType in listOf(MessageType.VISITOR_MESSAGE.valueType, MessageType.RECEIVED_BY_MEDIATO.valueType, MessageType.RECEIVED_BY_OPERATOR.valueType))  && networkMessage.isFile -> {
                networkMessage.attachmentUrl?.let { url ->
                    TransferFileInfo(
                        size = getWeightFile(url) ?: getWeightMediaFile(context, url)
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

    override suspend fun searchTimestampsMessages(uuid: String, searchText: String): NetworkSearch? {
        return messageApi.searchMessages(
            NetworkBodySearch(
                visitorUuid = uuid,
                searchText = searchText
            )
        ).toData()
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

    override suspend fun selectButton(messageId: String, actionId: String, buttonId: String) {
        socketApi.selectAction(actionId)
        messagesDao.getMessageById(messageId)?.let {
            val updatedButtons = it.keyboard?.buttons?.map { horizontalButtons ->
                horizontalButtons.map { button: ButtonEntity ->
                    ButtonEntity(
                        buttonId = button.buttonId,
                        title = button.title,
                        action = button.action,
                        typeOperation = button.typeOperation,
                        color = button.color,
                        image = button.image,
                        imageEmoji = button.imageEmoji,
                        hasFullSize = button.hasFullSize,
                        selected = button.buttonId == buttonId
                    )
                }
            }
            messagesDao.selectButton(messageId, KeyboardEntity(updatedButtons ?: listOf()))
        }
    }

    override fun selectButtonInWidget(actionId: String) {
        socketApi.selectAction(actionId)
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

    override fun removeAllInfoMessages() {
        messagesDao.deleteAllMessageByType(MessageType.INFO_MESSAGE.valueType)
    }

    override fun removeAllMessages() {
        messagesDao.deleteAllMessages()
    }

    override fun setUpdateSearchMessagePosition(updateSearchMessagePosition: suspend (insertedMessages: List<MessageEntity>) -> Unit) {
        socketApi.setUpdateSearchMessagePosition(updateSearchMessagePosition)
    }
}