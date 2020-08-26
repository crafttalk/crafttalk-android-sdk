package com.crafttalk.chat.presentation.helper.mappers

import android.content.Context
import com.crafttalk.chat.R
import com.crafttalk.chat.data.local.db.entity.Message
import com.crafttalk.chat.domain.entity.message.Action
import com.crafttalk.chat.domain.entity.message.MessageType.Companion.getMessageTypeByValueType
import com.crafttalk.chat.presentation.helper.converters.convertToSpannableString
import com.crafttalk.chat.presentation.model.*

fun messageModelMapper(listLocalMessage: List<Message>, context: Context): List<MessageModel> {
    return listLocalMessage.mapNotNull {
        when (true) {
            (it.message != null && it.message.isNotEmpty()) && (it.attachmentUrl == null) -> TextMessageItem(
                it.id,
                if (it.isReply) Role.OPERATOR else Role.USER,
                it.timestamp,
                it.message.convertToSpannableString(it.spanStructureList, context),
                it.actions?.let { listAction -> actionModelMapper(listAction) },
                if (it.isReply) it.operatorName ?: "Бот" else "Вы",
                getMessageTypeByValueType(it.messageType)
            )
            (it.message == null || it.message.isEmpty()) && (it.attachmentUrl != null && it.attachmentName != null) && (it.attachmentType == "IMAGE") && (it.attachmentName.contains(".GIF", true)) -> GifMessageItem(
                it.id,
                it.idKey,
                if (it.isReply) Role.OPERATOR else Role.USER,
                it.attachmentUrl,
                it.attachmentName,
                it.timestamp,
                if (it.isReply) it.operatorName ?: "Бот" else "Вы",
                getMessageTypeByValueType(it.messageType),
                it.height ?: 0,
                it.width ?: 0
            )
            (it.message == null || it.message.isEmpty()) && (it.attachmentUrl != null && it.attachmentName != null) && (it.attachmentType == "IMAGE") -> ImageMessageItem(
                it.id,
                it.idKey,
                if (it.isReply) Role.OPERATOR else Role.USER,
                it.attachmentUrl,
                it.attachmentName,
                it.timestamp,
                if (it.isReply) it.operatorName ?: "Бот" else "Вы",
                getMessageTypeByValueType(it.messageType),
                it.height ?: 0,
                it.width ?: 0
            )
            (it.message == null || it.message.isEmpty()) && (it.attachmentUrl != null && it.attachmentName != null) && (it.attachmentType == "FILE") -> FileMessageItem(
                it.id,
                if (it.isReply) Role.OPERATOR else Role.USER,
                it.attachmentUrl,
                it.attachmentName,
                it.timestamp,
                if (it.isReply) it.operatorName ?: "Бот" else "Вы",
                getMessageTypeByValueType(it.messageType)
            )
            else -> null
        }
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