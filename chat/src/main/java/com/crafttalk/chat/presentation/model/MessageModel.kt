package com.crafttalk.chat.presentation.model

import android.text.SpannableString
import com.crafttalk.chat.domain.entity.message.Action
import com.crafttalk.chat.domain.entity.message.MessageType as StateMessage
import com.crafttalk.chat.domain.entity.message.MessageType.DEFAULT

sealed class MessageModel(open val typeMessage: MessageType, open val timestamp: Long, open val authorName: String, open val stateCheck: StateMessage)
data class DefaultMessage(
    val id: String,
    override val timestamp: Long
) : MessageModel(MessageType.DEFAULT_MESSAGE, timestamp, "", DEFAULT)
data class TextMessage(
    override val typeMessage: MessageType,
    override val timestamp: Long,
    val message: SpannableString,
    val actions: List<Action>?,
    override val authorName: String,
    override val stateCheck: StateMessage
) : MessageModel(typeMessage, timestamp, authorName, stateCheck)
data class ImageMessage(
    val idKey: Long,
    override val typeMessage: MessageType,
    val imageUrl: String,
    val imageName: String,
    override val timestamp: Long,
    override val authorName: String,
    override val stateCheck: StateMessage,
    val height: Int,
    val width: Int
) : MessageModel(typeMessage, timestamp, authorName, stateCheck)
data class GifMessage(
    val idKey: Long,
    override val typeMessage: MessageType,
    val gifUrl: String,
    val gifName: String,
    override val timestamp: Long,
    override val authorName: String,
    override val stateCheck: StateMessage,
    val height: Int,
    val width: Int
) : MessageModel(typeMessage, timestamp, authorName, stateCheck)
data class FileMessage(
    override val typeMessage: MessageType,
    val fileUrl: String,
    val fileName: String,
    override val timestamp: Long,
    override val authorName: String,
    override val stateCheck: StateMessage
) : MessageModel(typeMessage, timestamp, authorName, stateCheck)