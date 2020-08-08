package com.crafttalk.chat.presentation.helper.mappers

import android.content.Context
import com.crafttalk.chat.data.local.db.entity.Message
import com.crafttalk.chat.domain.entity.message.MessageType.Companion.getMessageTypeByValueType
import com.crafttalk.chat.presentation.helper.converters.convertToSpannableString
import com.crafttalk.chat.presentation.model.*

fun messageModelMapper(listLocalMessage: List<Message>, context: Context): List<MessageModel> {
    return listLocalMessage.map {
        when (true) {
            (it.message != null && it.message.isNotEmpty()) && (it.attachmentUrl == null) -> TextMessage(
                if (it.isReply) MessageType.OPERATOR_TEXT_MESSAGE else MessageType.USER_TEXT_MESSAGE,
                it.timestamp,
                it.message.convertToSpannableString(it.spanStructureList, context),
                it.actions,
                if (it.isReply) it.operatorName ?: "Бот" else "Вы",
                getMessageTypeByValueType(it.messageType)
            )
            (it.message == null || it.message.isEmpty()) && (it.attachmentUrl != null && it.attachmentName != null) && (it.attachmentType == "IMAGE") && (it.attachmentName.contains(".GIF", true)) -> GifMessage(
                it.idKey,
                if (it.isReply) MessageType.OPERATOR_GIF_MESSAGE else MessageType.USER_GIF_MESSAGE,
                it.attachmentUrl,
                it.attachmentName,
                it.timestamp,
                if (it.isReply) it.operatorName ?: "Бот" else "Вы",
                getMessageTypeByValueType(it.messageType),
                it.height ?: 0,
                it.width ?: 0
            )
            (it.message == null || it.message.isEmpty()) && (it.attachmentUrl != null && it.attachmentName != null) && (it.attachmentType == "IMAGE") -> ImageMessage(
                it.idKey,
                if (it.isReply) MessageType.OPERATOR_IMAGE_MESSAGE else MessageType.USER_IMAGE_MESSAGE,
                it.attachmentUrl,
                it.attachmentName,
                it.timestamp,
                if (it.isReply) it.operatorName ?: "Бот" else "Вы",
                getMessageTypeByValueType(it.messageType),
                it.height ?: 0,
                it.width ?: 0
            )
            (it.message == null || it.message.isEmpty()) && (it.attachmentUrl != null && it.attachmentName != null) && (it.attachmentType == "FILE") -> FileMessage(
                if (it.isReply) MessageType.OPERATOR_FILE_MESSAGE else MessageType.USER_FILE_MESSAGE,
                it.attachmentUrl,
                it.attachmentName,
                it.timestamp,
                if (it.isReply) it.operatorName ?: "Бот" else "Вы",
                getMessageTypeByValueType(it.messageType)
            )
            else -> DefaultMessage(
                it.id,
                it.timestamp
            )
        }
    }
}
