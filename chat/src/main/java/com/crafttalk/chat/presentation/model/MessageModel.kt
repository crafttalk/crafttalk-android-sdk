package com.crafttalk.chat.presentation.model

import android.text.SpannableString
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseItem
import com.crafttalk.chat.presentation.model.Role.*
import com.crafttalk.chat.domain.entity.message.MessageType as StateMessage

sealed class MessageModel(
    open val id: String,
    open val role: Role,
    open val timestamp: Long,
    open val authorName: String,
    open val authorPreview: String? = null,
    open val stateCheck: StateMessage,
    var isFirstMessageInDay: Boolean = false
) : BaseItem() {
    override fun <T : BaseItem> isSame(item: T): Boolean {
        return item is MessageModel && item.id == id
    }
}

data class StickerMessageItem(
    override val id: String,
    override val role: Role,
    val sticker: FileModel,
    override val timestamp: Long,
    override val authorName: String,
    override val authorPreview: String?,
    override val stateCheck: StateMessage
) : MessageModel(id, role, timestamp, authorName, authorPreview, stateCheck) {
    override fun getLayout(): Int {
        return when(role) {
            USER -> R.layout.com_crafttalk_chat_item_user_sticker_message
            OPERATOR -> R.layout.com_crafttalk_chat_item_server_sticker_message
            NEUTRAL -> R.layout.com_crafttalk_chat_item_default_message
        }
    }
}

data class DefaultMessageItem(
    override val id: String,
    override val timestamp: Long
) : MessageModel(id, NEUTRAL, timestamp, "", null, StateMessage.DEFAULT, false) {
    override fun getLayout(): Int = R.layout.com_crafttalk_chat_item_default_message
}

data class TextMessageItem(
    override val id: String,
    override val role: Role,
    val message: SpannableString,
    val actions: List<ActionItem>?,
    val hasSelectedAction: Boolean,
    val buttons: List<ButtonsListItem>?,
    val hasSelectedButton: Boolean,
    val repliedMessage: RepliedMessageModel?,
    override val timestamp: Long,
    override val authorName: String,
    override val authorPreview: String?,
    override val stateCheck: StateMessage
) : MessageModel(id, role, timestamp, authorName, authorPreview, stateCheck) {
    override fun getLayout(): Int {
        return when(role) {
            USER -> R.layout.com_crafttalk_chat_item_user_text_message
            OPERATOR -> R.layout.com_crafttalk_chat_item_server_text_message
            NEUTRAL -> R.layout.com_crafttalk_chat_item_default_message
        }
    }
}

data class ImageMessageItem(
    override val id: String,
    override val role: Role,
    val image: FileModel,
    override val timestamp: Long,
    override val authorName: String,
    override val authorPreview: String?,
    override val stateCheck: StateMessage
) : MessageModel(id, role, timestamp, authorName, authorPreview, stateCheck) {
    override fun getLayout(): Int {
        return when(role) {
            USER -> R.layout.com_crafttalk_chat_item_user_image_message
            OPERATOR -> R.layout.com_crafttalk_chat_item_server_image_message
            NEUTRAL -> R.layout.com_crafttalk_chat_item_default_message
        }
    }
}

data class GifMessageItem(
    override val id: String,
    override val role: Role,
    val gif: FileModel,
    override val timestamp: Long,
    override val authorName: String,
    override val authorPreview: String?,
    override val stateCheck: StateMessage
) : MessageModel(id, role, timestamp, authorName, authorPreview, stateCheck) {
    override fun getLayout(): Int {
        return when(role) {
            USER -> R.layout.com_crafttalk_chat_item_user_gif_message
            OPERATOR -> R.layout.com_crafttalk_chat_item_server_gif_message
            NEUTRAL -> R.layout.com_crafttalk_chat_item_default_message
        }
    }
}

data class FileMessageItem(
    override val id: String,
    override val role: Role,
    val document: FileModel,
    override val timestamp: Long,
    override val authorName: String,
    override val authorPreview: String?,
    override val stateCheck: StateMessage
) : MessageModel(id, role, timestamp, authorName, authorPreview, stateCheck) {
    override fun getLayout() : Int {
        return when(role) {
            USER -> R.layout.com_crafttalk_chat_item_user_file_message
            OPERATOR -> R.layout.com_crafttalk_chat_item_server_file_message
            NEUTRAL -> R.layout.com_crafttalk_chat_item_default_message
        }
    }
}

data class UnionMessageItem(
    override val id: String,
    override val role: Role,
    val message: SpannableString,
    val actions: List<ActionItem>?,
    val hasSelectedAction: Boolean,
    val buttons: List<ButtonsListItem>?,
    val hasSelectedButton: Boolean,
    val file: FileModel,
    override val timestamp: Long,
    override val authorName: String,
    override val authorPreview: String?,
    override val stateCheck: StateMessage
) : MessageModel(id, role, timestamp, authorName, authorPreview, stateCheck) {
    override fun getLayout() : Int {
        return when(role) {
            USER -> R.layout.com_crafttalk_chat_item_user_union_message
            OPERATOR -> R.layout.com_crafttalk_chat_item_server_union_message
            NEUTRAL -> R.layout.com_crafttalk_chat_item_default_message
        }
    }
}

data class TransferMessageItem(
    override val id: String,
    override val timestamp: Long,
    override val authorName: String,
    override val authorPreview: String?
) : MessageModel(id, NEUTRAL, timestamp, authorName, authorPreview, StateMessage.CONNECTED_OPERATOR) {
    override fun getLayout(): Int = R.layout.com_crafttalk_chat_item_transfer_message
}

data class InfoMessageItem(
    override val id: String,
    val message: SpannableString,
    override val timestamp: Long
) : MessageModel(id, NEUTRAL, timestamp, "", null, StateMessage.INFO_MESSAGE) {
    override fun getLayout(): Int = R.layout.com_crafttalk_chat_item_info_message
}

data class WidgetMessageItem(
    override val id: String,
    val message: SpannableString?,
    val widgetId: String,
    val payload: Any,
    override val timestamp: Long,
    override val authorName: String,
    override val authorPreview: String?,
    override val stateCheck: StateMessage
) : MessageModel(id, OPERATOR, timestamp, authorName, authorPreview, stateCheck) {
    override fun getLayout(): Int = R.layout.com_crafttalk_chat_item_server_widget_message
}