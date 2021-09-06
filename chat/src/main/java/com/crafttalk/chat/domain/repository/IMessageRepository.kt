package com.crafttalk.chat.domain.repository

import android.content.Context
import androidx.paging.DataSource
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.message.NetworkMessage
import com.crafttalk.chat.domain.transfer.TransferFileInfo

interface IMessageRepository {

    fun getMessages(uuid: String): DataSource.Factory<Int, MessageEntity>

    fun getCountUnreadMessages(uuid: String, currentReadMessageTime: Long): Int?

    // получение времени первого сообщения
    suspend fun getTimeFirstMessage(uuid: String): Long?

    // получение времени последнего сообщения
    suspend fun getTimeLastMessage(uuid: String): Long?

    // загрузка определенного пула сообщений
    suspend fun uploadMessages(
        uuid: String,
        token: String,
        startTime: Long?,
        endTime: Long,
        updateReadPoint: (newTimeMark: Long) -> Boolean,
        returnedEmptyPool: () -> Unit,
        getPersonPreview: suspend (personId: String) -> String?,
        getFileInfo: suspend (context: Context, token: String, networkMessage: NetworkMessage) -> TransferFileInfo?
    ): List<MessageEntity>

    suspend fun mergeNewMessages()

    suspend fun updatePersonNames(
        messages: List<MessageEntity>,
        updatePersonName: suspend (personId: String?, currentPersonName: String?) -> Unit
    )

    suspend fun getFileInfo(
        context: Context,
        token: String,
        networkMessage: NetworkMessage
    ): TransferFileInfo?

    suspend fun sendMessages(message: String)
    suspend fun selectAction(uuid: String, messageId: String, actionId: String)

    fun updateSizeMessage(uuid: String, id: String, height: Int, width: Int)

}