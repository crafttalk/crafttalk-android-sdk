package com.crafttalk.chat.presentation.helper.mappers

import android.content.Context
import android.text.SpannableString
import com.crafttalk.chat.R
import com.crafttalk.chat.data.local.db.entity.ActionEntity
import com.crafttalk.chat.data.local.db.entity.ButtonEntity
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.file.TypeDownloadProgress
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.message.MessageType
import com.crafttalk.chat.domain.entity.message.MessageType.Companion.getMessageTypeByValueType
import com.crafttalk.chat.domain.entity.message.NetworkButtonColor
import com.crafttalk.chat.presentation.helper.converters.convertToSpannableString
import com.crafttalk.chat.presentation.model.*
import com.crafttalk.chat.utils.ChatAttr

fun messageModelMapper(localMessage: MessageEntity, context: Context): MessageModel {
    return when {
        localMessage.messageType == MessageType.CONNECTED_OPERATOR.valueType -> TransferMessageItem(
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
        localMessage.widget != null && localMessage.messageType == MessageType.MESSAGE.valueType -> WidgetMessageItem(
            id = localMessage.id,
            message = localMessage.message?.convertToSpannableString(false, localMessage.spanStructureList, context),
            widgetId = localMessage.widget.widgetId,
            payload = localMessage.widget.payload,
            timestamp = localMessage.timestamp,
            authorName = localMessage.operatorName ?: "Бот",
            authorPreview = localMessage.operatorPreview,
            stateCheck = getMessageTypeByValueType(localMessage.messageType)
        )
        (localMessage.message != null && localMessage.message.isNotEmpty()) && (localMessage.attachmentUrl == null) -> TextMessageItem(
            id = localMessage.id,
            role = if (localMessage.isReply) Role.OPERATOR else Role.USER,
            message = localMessage.message.convertToSpannableString(!localMessage.isReply, localMessage.spanStructureList, context),
            actions = localMessage.actions?.let { listActions -> actionModelMapper(listActions) },
            hasSelectedAction = localMessage.hasSelectedAction(),
            buttons = localMessage.keyboard?.buttons?.let { listButtons -> buttonModelMapper(listButtons) },
            hasSelectedButton = localMessage.hasSelectedButton(),
            repliedMessage = RepliedMessageModel.map(localMessage, context),
            timestamp = localMessage.timestamp,
            authorName = if (localMessage.isReply) localMessage.operatorName ?: "Бот" else "Вы",
            authorPreview = if (localMessage.isReply) localMessage.operatorPreview else null,
            stateCheck = getMessageTypeByValueType(localMessage.messageType)
        )

        (localMessage.message == null || localMessage.message.isEmpty()) && localMessage.attachmentType == TypeFile.STICKER -> StickerMessageItem(
            id = localMessage.id,
            role = if (localMessage.isReply) Role.OPERATOR else Role.USER,
            sticker = FileModel(
                url = localMessage.attachmentUrl!!,
                name = SpannableString(localMessage.attachmentName ?: ""),
                size = localMessage.attachmentSize,
                height = localMessage.height,
                width = localMessage.width,
                failLoading = localMessage.height == null || localMessage.height == 0 || localMessage.width == null || localMessage.width == 0,
                type = localMessage.attachmentType
            ),
            timestamp = localMessage.timestamp,
            authorName = if (localMessage.isReply) localMessage.operatorName ?: "Бот" else "Вы",
            authorPreview = if (localMessage.isReply) localMessage.operatorPreview else null,
            stateCheck = getMessageTypeByValueType(localMessage.messageType)
        )

        (localMessage.message == null || localMessage.message.isEmpty()) && localMessage.attachmentType == TypeFile.GIF -> GifMessageItem(
            localMessage.id,
            if (localMessage.isReply) Role.OPERATOR else Role.USER,
            FileModel(
                url = localMessage.attachmentUrl!!,
                name = SpannableString(localMessage.attachmentName ?: ""),
                size = localMessage.attachmentSize,
                height = localMessage.height,
                width = localMessage.width,
                failLoading = localMessage.height == null || localMessage.height == 0 || localMessage.width == null || localMessage.width == 0
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
                name = SpannableString(localMessage.attachmentName ?: ""),
                size = localMessage.attachmentSize,
                height = localMessage.height,
                width = localMessage.width,
                failLoading = localMessage.height == null || localMessage.height == 0 || localMessage.width == null || localMessage.width == 0
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
                name = SpannableString(localMessage.attachmentName ?: ""),
                size = localMessage.attachmentSize,
                typeDownloadProgress = localMessage.attachmentDownloadProgressType ?: TypeDownloadProgress.NOT_DOWNLOADED
            ),
            localMessage.timestamp,
            if (localMessage.isReply) localMessage.operatorName ?: "Бот" else "Вы",
            if (localMessage.isReply) localMessage.operatorPreview else null,
            getMessageTypeByValueType(localMessage.messageType)
        )
        (localMessage.message != null && localMessage.message.isNotEmpty()) && (!localMessage.attachmentUrl.isNullOrEmpty() && !localMessage.attachmentName.isNullOrEmpty() && localMessage.attachmentType != null) -> UnionMessageItem(
            id = localMessage.id,
            role = if (localMessage.isReply) Role.OPERATOR else Role.USER,
            message = localMessage.message.convertToSpannableString(!localMessage.isReply, localMessage.spanStructureList, context),
            actions = localMessage.actions?.let { listActions -> actionModelMapper(listActions) },
            hasSelectedAction = localMessage.hasSelectedAction(),
            buttons = localMessage.keyboard?.buttons?.let { listButtons -> buttonModelMapper(listButtons) },
            hasSelectedButton = localMessage.hasSelectedButton(),
            file = FileModel(
                url = localMessage.attachmentUrl,
                name = SpannableString(localMessage.attachmentName),
                height = localMessage.height,
                width = localMessage.width,
                size = localMessage.attachmentSize,
                failLoading = (localMessage.attachmentType in listOf( TypeFile.IMAGE, TypeFile.GIF)) && (localMessage.height == null || localMessage.height == 0 || localMessage.width == null || localMessage.width == 0),
                type = localMessage.attachmentType,
                typeDownloadProgress = localMessage.attachmentDownloadProgressType ?: TypeDownloadProgress.NOT_DOWNLOADED
            ),
            timestamp = localMessage.timestamp,
            authorName = if (localMessage.isReply) localMessage.operatorName ?: "Бот" else "Вы",
            authorPreview = if (localMessage.isReply) localMessage.operatorPreview else null,
            stateCheck = getMessageTypeByValueType(localMessage.messageType)
        )
        else -> DefaultMessageItem(
            localMessage.id,
            localMessage.timestamp
        )
    }
}

fun actionModelMapper(listActions: List<ActionEntity>): List<ActionItem>? {
    if (listActions.isEmpty()) return null
    return listActions.mapIndexed { position, action ->
        val backgroundRes = if (listActions.size == 1) {
            R.drawable.com_crafttalk_chat_background_single_item_action
        } else {
            when (position) {
                0 -> R.drawable.com_crafttalk_chat_background_top_item_action
                listActions.size - 1 -> R.drawable.com_crafttalk_chat_background_bottom_item_action
                else -> R.drawable.com_crafttalk_chat_background_item_action
            }
        }
        ActionItem(action.actionId, action.actionText, action.isSelected, backgroundRes)
    }
}

fun buttonModelMapper(listButtons: List<List<ButtonEntity>>): List<ButtonsListItem>? {
    if (listButtons.isEmpty()) return null
    return listButtons.mapIndexed { positionVerical, horizontalButtons ->
        ButtonsListItem(
            horizontalButtons.mapIndexed { positionHorizontal, button ->
                val textColor = when {
                    button.color == NetworkButtonColor.PRIMARY && button.selected -> ChatAttr.getInstance().colorPrimaryTextOperatorSelectedButton
                    button.color == NetworkButtonColor.SECONDARY && button.selected -> ChatAttr.getInstance().colorSecondaryTextOperatorSelectedButton
                    button.color == NetworkButtonColor.NEGATIVE && button.selected -> ChatAttr.getInstance().colorNegativeTextOperatorSelectedButton
                    button.color == NetworkButtonColor.PRIMARY && !button.selected -> ChatAttr.getInstance().colorPrimaryTextOperatorButton
                    button.color == NetworkButtonColor.SECONDARY && !button.selected -> ChatAttr.getInstance().colorSecondaryTextOperatorButton
                    button.color == NetworkButtonColor.NEGATIVE && !button.selected -> ChatAttr.getInstance().colorNegativeTextOperatorButton
                    button.selected -> ChatAttr.getInstance().colorTextOperatorSelectedButton
                    !button.selected -> ChatAttr.getInstance().colorTextOperatorButton
                    else -> ChatAttr.getInstance().colorTextOperatorButton
                }
                val backgroundRes = when {
                    button.color == NetworkButtonColor.PRIMARY && button.selected -> ChatAttr.getInstance().backgroundPrimaryResOperatorSelectedButton
                    button.color == NetworkButtonColor.SECONDARY && button.selected -> ChatAttr.getInstance().backgroundSecondaryResOperatorSelectedButton
                    button.color == NetworkButtonColor.NEGATIVE && button.selected -> ChatAttr.getInstance().backgroundNegativeResOperatorSelectedButton
                    button.color == NetworkButtonColor.PRIMARY && !button.selected -> ChatAttr.getInstance().backgroundPrimaryResOperatorButton
                    button.color == NetworkButtonColor.SECONDARY && !button.selected -> ChatAttr.getInstance().backgroundSecondaryResOperatorButton
                    button.color == NetworkButtonColor.NEGATIVE && !button.selected -> ChatAttr.getInstance().backgroundNegativeResOperatorButton
                    button.selected -> ChatAttr.getInstance().backgroundResOperatorSelectedButton
                    !button.selected -> ChatAttr.getInstance().backgroundResOperatorButton
                    else -> ChatAttr.getInstance().backgroundResOperatorButton
                }

                val widthItem = (
                        ChatAttr.getInstance().widthItemOperatorTextMessage -
                                (horizontalButtons.size - 1) * ChatAttr.getInstance().horizontalSpacingOperatorButton
                        ) / horizontalButtons.size

                ButtonItem(
                    id = button.buttonId,
                    text = button.title,
                    action = button.action,
                    typeOperation = button.typeOperation,
                    isSelected = button.selected,
                    imageUrl = button.image?.url,
                    imageEmoji = button.imageEmoji,
                    textColor = textColor,
                    backgroundRes = backgroundRes,
                    width = widthItem.toInt(),
                    marginTop = if (positionVerical == 0) 0 else (ChatAttr.getInstance().verticalSpacingOperatorButton / 2).toInt(),
                    marginBottom = if (positionVerical == listButtons.size - 1) 0 else (ChatAttr.getInstance().verticalSpacingOperatorButton / 2).toInt(),
                    marginStart = if (positionHorizontal == 0) 0 else (ChatAttr.getInstance().horizontalSpacingOperatorButton / 2).toInt(),
                    marginEnd = if (positionHorizontal == horizontalButtons.size - 1) 0 else (ChatAttr.getInstance().horizontalSpacingOperatorButton / 2).toInt()
                )
            }
        )
    }
}