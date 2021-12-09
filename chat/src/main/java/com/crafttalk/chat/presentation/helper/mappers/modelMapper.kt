package com.crafttalk.chat.presentation.helper.mappers

import android.content.Context
import com.crafttalk.chat.R
import com.crafttalk.chat.data.local.db.entity.ActionEntity
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.file.TypeDownloadProgress
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.message.MessageType
import com.crafttalk.chat.domain.entity.message.MessageType.Companion.getMessageTypeByValueType
import com.crafttalk.chat.presentation.helper.converters.convertToSpannableString
import com.crafttalk.chat.presentation.model.*

fun messageModelMapper(localMessage: MessageEntity, context: Context): MessageModel {
    return when {
        localMessage.messageType == MessageType.TRANSFER_TO_OPERATOR.valueType -> TransferMessageItem(
            localMessage.id,
            localMessage.timestamp,
            if (localMessage.isReply) localMessage.operatorName ?: "Бот" else "Вы",
            localMessage.operatorPreview
        )
        localMessage.message != null && localMessage.messageType == MessageType.INFO_MESSAGE.valueType -> InfoMessageItem(
            localMessage.id,
            localMessage.message.convertToSpannableString(false, localMessage.spanStructureList, context),
            localMessage.timestamp
        )
        (localMessage.message != null && localMessage.message.isNotEmpty()) && (localMessage.attachmentUrl == null) -> TextMessageItem(
            localMessage.id,
            if (localMessage.isReply) Role.OPERATOR else Role.USER,
            localMessage.message.convertToSpannableString(!localMessage.isReply, localMessage.spanStructureList, context),
            localMessage.actions?.let { listAction -> actionModelMapper(listAction) },
            localMessage.hasSelectedAction(),
            repliedMessage = RepliedMessageModel.map(localMessage, context),
            localMessage.timestamp,
            if (localMessage.isReply) localMessage.operatorName ?: "Бот" else "Вы",
            if (localMessage.isReply) localMessage.operatorPreview else null,
            getMessageTypeByValueType(localMessage.messageType)
        )
        (localMessage.message == null || localMessage.message.isEmpty()) && localMessage.attachmentType == TypeFile.GIF -> GifMessageItem(
            localMessage.id,
            if (localMessage.isReply) Role.OPERATOR else Role.USER,
            FileModel(
                url = localMessage.attachmentUrl!!,
                name = localMessage.attachmentName ?: "",
                size = localMessage.attachmentSize,
                height = localMessage.height,
                width = localMessage.width,
                failLoading = localMessage.height == null || localMessage.width == null
            ),
            localMessage.timestamp,
            if (localMessage.isReply) localMessage.operatorName ?: "Бот" else "Вы",
            if (localMessage.isReply) localMessage.operatorPreview else null,
            getMessageTypeByValueType(localMessage.messageType)
        )
        (localMessage.message == null || localMessage.message.isEmpty()) && localMessage.attachmentType == TypeFile.IMAGE -> ImageMessageItem(
            localMessage.id,
            if (localMessage.isReply) Role.OPERATOR else Role.USER,
            FileModel(
                url = localMessage.attachmentUrl!!,
                name = localMessage.attachmentName ?: "",
                size = localMessage.attachmentSize,
                height = localMessage.height,
                width = localMessage.width,
                failLoading = localMessage.height == null || localMessage.width == null
            ),
            localMessage.timestamp,
            if (localMessage.isReply) localMessage.operatorName ?: "Бот" else "Вы",
            if (localMessage.isReply) localMessage.operatorPreview else null,
            getMessageTypeByValueType(localMessage.messageType)
        )
        (localMessage.message == null || localMessage.message.isEmpty()) && localMessage.attachmentType == TypeFile.FILE -> FileMessageItem(
            localMessage.id,
            if (localMessage.isReply) Role.OPERATOR else Role.USER,
            FileModel(
                url = localMessage.attachmentUrl!!,
                name = localMessage.attachmentName ?: "",
                size = localMessage.attachmentSize,
                typeDownloadProgress = localMessage.attachmentDownloadProgressType ?: TypeDownloadProgress.NOT_DOWNLOADED
            ),
            localMessage.timestamp,
            if (localMessage.isReply) localMessage.operatorName ?: "Бот" else "Вы",
            if (localMessage.isReply) localMessage.operatorPreview else null,
            getMessageTypeByValueType(localMessage.messageType)
        )
        (localMessage.message != null && localMessage.message.isNotEmpty()) && (!localMessage.attachmentUrl.isNullOrEmpty() && !localMessage.attachmentName.isNullOrEmpty() && localMessage.attachmentType != null) -> UnionMessageItem(
            localMessage.id,
            if (localMessage.isReply) Role.OPERATOR else Role.USER,
            localMessage.message.convertToSpannableString(!localMessage.isReply, localMessage.spanStructureList, context),
            localMessage.actions?.let { listAction -> actionModelMapper(listAction) },
            localMessage.hasSelectedAction(),
            FileModel(
                url = localMessage.attachmentUrl,
                name = localMessage.attachmentName,
                height = localMessage.height,
                width = localMessage.width,
                size = localMessage.attachmentSize,
                failLoading = (localMessage.attachmentType in listOf( TypeFile.IMAGE, TypeFile.GIF)) && (localMessage.height == null || localMessage.width == null),
                type = localMessage.attachmentType,
                typeDownloadProgress = localMessage.attachmentDownloadProgressType ?: TypeDownloadProgress.NOT_DOWNLOADED
            ),
            localMessage.timestamp,
            if (localMessage.isReply) localMessage.operatorName ?: "Бот" else "Вы",
            if (localMessage.isReply) localMessage.operatorPreview else null,
            getMessageTypeByValueType(localMessage.messageType)
        )
        else -> DefaultMessageItem(
            localMessage.id,
            localMessage.timestamp
        )
    }
}

fun actionModelMapper(listAction: List<ActionEntity>): List<ActionItem>? {
    if (listAction.isEmpty()) return null
    return listAction.mapIndexed { position, action ->
        val backgroundRes = if (listAction.size == 1) {
            R.drawable.com_crafttalk_chat_background_single_item_action
        } else {
            when (position) {
                0 -> R.drawable.com_crafttalk_chat_background_top_item_action
                listAction.size - 1 -> R.drawable.com_crafttalk_chat_background_bottom_item_action
                else -> R.drawable.com_crafttalk_chat_background_item_action
            }
        }
        ActionItem(action.actionId, action.actionText, action.isSelected, backgroundRes)
    }
}