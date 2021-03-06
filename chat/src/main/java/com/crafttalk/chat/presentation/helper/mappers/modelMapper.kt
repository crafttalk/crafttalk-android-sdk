package com.crafttalk.chat.presentation.helper.mappers

import android.content.Context
import com.crafttalk.chat.R
import com.crafttalk.chat.data.local.db.entity.Message
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.message.Action
import com.crafttalk.chat.domain.entity.message.MessageType.Companion.getMessageTypeByValueType
import com.crafttalk.chat.presentation.helper.converters.convertToSpannableString
import com.crafttalk.chat.presentation.model.*

fun messageModelMapper(localMessage: Message, context: Context): MessageModel? {
    return when {
        (localMessage.message != null && localMessage.message.isNotEmpty()) && (localMessage.attachmentUrl == null) -> TextMessageItem(
            localMessage.id,
            if (localMessage.isReply) Role.OPERATOR else Role.USER,
            localMessage.message.convertToSpannableString(localMessage.spanStructureList, context),
            localMessage.actions?.let { listAction -> actionModelMapper(listAction) },
            localMessage.timestamp,
            if (localMessage.isReply) localMessage.operatorName ?: "Бот" else "Вы",
            if (localMessage.isReply) localMessage.operatorPreview else null,
            getMessageTypeByValueType(localMessage.messageType),
            localMessage.isRead
        )
        (localMessage.message == null || localMessage.message.isEmpty()) && (localMessage.attachmentUrl != null && localMessage.attachmentName != null) && (localMessage.attachmentType == "IMAGE") && (localMessage.attachmentName.contains(".GIF", true)) -> GifMessageItem(
            localMessage.id,
            localMessage.idKey,
            if (localMessage.isReply) Role.OPERATOR else Role.USER,
            FileModel(
                localMessage.attachmentUrl,
                localMessage.attachmentName,
                height = localMessage.height ?: 0,
                width = localMessage.width ?: 0
            ),
            localMessage.timestamp,
            if (localMessage.isReply) localMessage.operatorName ?: "Бот" else "Вы",
            if (localMessage.isReply) localMessage.operatorPreview else null,
            getMessageTypeByValueType(localMessage.messageType),
            localMessage.isRead
        )
        (localMessage.message == null || localMessage.message.isEmpty()) && (localMessage.attachmentUrl != null && localMessage.attachmentName != null) && (localMessage.attachmentType == "IMAGE") -> ImageMessageItem(
            localMessage.id,
            localMessage.idKey,
            if (localMessage.isReply) Role.OPERATOR else Role.USER,
            FileModel(
                localMessage.attachmentUrl,
                localMessage.attachmentName,
                height = localMessage.height ?: 0,
                width = localMessage.width ?: 0
            ),
            localMessage.timestamp,
            if (localMessage.isReply) localMessage.operatorName ?: "Бот" else "Вы",
            if (localMessage.isReply) localMessage.operatorPreview else null,
            getMessageTypeByValueType(localMessage.messageType),
            localMessage.isRead
        )
        (localMessage.message == null || localMessage.message.isEmpty()) && (localMessage.attachmentUrl != null && localMessage.attachmentName != null) && (localMessage.attachmentType == "FILE") -> FileMessageItem(
            localMessage.id,
            if (localMessage.isReply) Role.OPERATOR else Role.USER,
            FileModel(
                localMessage.attachmentUrl,
                localMessage.attachmentName
            ),
            localMessage.timestamp,
            if (localMessage.isReply) localMessage.operatorName ?: "Бот" else "Вы",
            if (localMessage.isReply) localMessage.operatorPreview else null,
            getMessageTypeByValueType(localMessage.messageType),
            localMessage.isRead
        )
        (localMessage.message != null && localMessage.message.isNotEmpty()) && (localMessage.attachmentUrl != null && localMessage.attachmentName != null) && ((localMessage.attachmentType == "FILE") || (localMessage.attachmentType == "IMAGE")) -> UnionMessageItem(
            localMessage.id,
            localMessage.idKey,
            if (localMessage.isReply) Role.OPERATOR else Role.USER,
            localMessage.message.convertToSpannableString(localMessage.spanStructureList, context),
            localMessage.actions?.let { listAction -> actionModelMapper(listAction) },
            FileModel(
                localMessage.attachmentUrl,
                localMessage.attachmentName,
                height = localMessage.height ?: 0,
                width = localMessage.width ?: 0,
                type = when {
                    localMessage.attachmentType == "FILE" -> TypeFile.FILE
                    (localMessage.attachmentType == "IMAGE") && (localMessage.attachmentName.contains(".GIF", true)) -> TypeFile.GIF
                    (localMessage.attachmentType == "IMAGE") && !(localMessage.attachmentName.contains(".GIF", true)) -> TypeFile.IMAGE
                    else -> null
                }
            ),
            localMessage.timestamp,
            if (localMessage.isReply) localMessage.operatorName ?: "Бот" else "Вы",
            if (localMessage.isReply) localMessage.operatorPreview else null,
            getMessageTypeByValueType(localMessage.messageType),
            localMessage.isRead
        )
        else -> DefaultMessageItem(
            localMessage.id,
            localMessage.timestamp
        )
    }
}

fun actionModelMapper(listAction: List<Action>): List<ActionItem>? {
    if (listAction.isEmpty()) return null
    return listAction.mapIndexed { position, action ->
        val backgroundRes = if (listAction.size == 1) {
            R.drawable.background_single_item_action
        } else {
            when (position) {
                0 -> R.drawable.background_top_item_action
                listAction.size - 1 -> R.drawable.background_bottom_item_action
                else -> R.drawable.background_item_action
            }
        }
        ActionItem(action.actionId, action.actionText, backgroundRes)
    }
}