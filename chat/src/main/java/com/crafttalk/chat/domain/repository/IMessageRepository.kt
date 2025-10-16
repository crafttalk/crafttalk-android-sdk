package com.crafttalk.chat.domain.repository

import android.content.Context
import androidx.paging.DataSource
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.file.TypeDownloadProgress
import com.crafttalk.chat.domain.entity.message.NetworkMessage
import com.crafttalk.chat.domain.entity.message.NetworkSearch
import com.crafttalk.chat.domain.transfer.TransferFileInfo

interface IMessageRepository {

    fun getMessages(): DataSource.Factory<Int, MessageEntity>

    fun getAllMessages(): List<MessageEntity>

    fun getCountUnreadMessages(
        currentReadMessageTime: Long,
        ignoredMessageTypes: List<Int>
    ): Int?

    suspend fun getPositionByTimestamp(id: String, timestamp: Long): Int?

    fun getTimestampMessageById(messageId: String): Long?

    fun getCountMessagesInclusiveTimestamp(timestampMessage: Long): Int?

    fun getCountUnreadMessagesRange(
        currentReadMessageTime: Long,
        timestampLastMessage: Long,
        ignoredMessageTypes: List<Int>
    ): Int?

    // получение времени первого сообщения
    suspend fun getTimeFirstMessage(): Long?

    // получение времени последнего сообщения
    suspend fun getTimeLastMessage(): Long?

    // загрузка определенного пула сообщений
    // [startTime; endTime)
    suspend fun uploadMessages(
        uuid: String,
        startTime: Long?,
        endTime: Long,
        updateReadPoint: (newTimeMarks: List<Pair<String, Long>>) -> Boolean,
        syncMessagesAcrossDevices: (countUnreadMessages: Int) -> Unit,
        allMessageLoaded: () -> Unit,
        notAllMessageLoaded: () -> Unit,
        getPersonPreview: suspend (personId: String) -> String?,
        getFileInfo: suspend (context: Context, networkMessage: NetworkMessage) -> TransferFileInfo?,
        updateSearchMessagePosition: suspend (insertedMessages: List<MessageEntity>) -> Unit
    ): List<MessageEntity>

    suspend fun mergeNewMessages()

    suspend fun updatePersonNames(
        messages: List<MessageEntity>,
        updatePersonName: suspend (personId: String?, currentPersonName: String?) -> Unit
    )

    suspend fun getFileInfo(
        context: Context,
        networkMessage: NetworkMessage
    ): TransferFileInfo?

    suspend fun sendMessages(message: String, repliedMessageId: String?)

    suspend fun sendServiceMessageUserIsTypingText(message: String)

    suspend fun sendServiceMessageUserStopTypingText()

    suspend fun readMessage(messageId: String)

    suspend fun searchTimestampsMessages(uuid: String, searchText: String): NetworkSearch?

    suspend fun selectAction(messageId: String, actionId: String)
    suspend fun selectButton(messageId: String, actionId: String, buttonId: String)
    fun selectButtonInWidget(actionId: String)

    fun updateSizeMessage(id: String, height: Int, width: Int)

    fun updateTypeDownloadProgressOfMessageWithAttachment(id: String, typeDownloadProgress: TypeDownloadProgress)

    fun removeAllInfoMessages()

    fun removeAllMessages()

    fun setUpdateSearchMessagePosition(updateSearchMessagePosition: suspend (insertedMessages: List<MessageEntity>) -> Unit)

    fun extractLinksFromHtmlMarkdown (networkMessages:MutableList<NetworkMessage>?): MutableList<NetworkMessage>

    fun checkCorrectImageName (networkMessages:MutableList<NetworkMessage>?): MutableList<NetworkMessage>
}