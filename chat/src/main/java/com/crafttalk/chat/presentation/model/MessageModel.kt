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
    open val stateCheck: StateMessage,
    var isFirstMessageInDay: Boolean = false
) : BaseItem() {
    override fun <T : BaseItem> isSame(item: T): Boolean {
        return item is MessageModel && item.id == id
    }
}

data class DefaultMessageItem(
    override val id: String,
    override val timestamp: Long
) : MessageModel(id, NEUTRAL, timestamp, "", StateMessage.DEFAULT) {
    override fun getLayout(): Int = R.layout.item_default_message
}

data class TextMessageItem(
    override val id: String,
    override val role: Role,
    val message: SpannableString,
    val actions: List<ActionItem>?,
    override val timestamp: Long,
    override val authorName: String,
    override val stateCheck: StateMessage
) : MessageModel(id, role, timestamp, authorName, stateCheck) {
    override fun getLayout(): Int {
        return when(role) {
            USER -> R.layout.item_user_text_message
            OPERATOR -> R.layout.item_server_text_message
            NEUTRAL -> R.layout.item_default_message
        }
    }
}

data class ImageMessageItem(
    override val id: String,
    val idKey: Long,
    override val role: Role,
    val image: FileModel,
    override val timestamp: Long,
    override val authorName: String,
    override val stateCheck: StateMessage
) : MessageModel(id, role, timestamp, authorName, stateCheck) {
    override fun getLayout(): Int {
        return when(role) {
            USER -> R.layout.item_user_image_message
            OPERATOR -> R.layout.item_server_image_message
            NEUTRAL -> R.layout.item_default_message
        }
    }
}

data class GifMessageItem(
    override val id: String,
    val idKey: Long,
    override val role: Role,
    val gif: FileModel,
    override val timestamp: Long,
    override val authorName: String,
    override val stateCheck: StateMessage
) : MessageModel(id, role, timestamp, authorName, stateCheck) {
    override fun getLayout(): Int {
        return when(role) {
            USER -> R.layout.item_user_gif_message
            OPERATOR -> R.layout.item_server_gif_message
            NEUTRAL -> R.layout.item_default_message
        }
    }
}

data class FileMessageItem(
    override val id: String,
    override val role: Role,
    val document: FileModel,
    override val timestamp: Long,
    override val authorName: String,
    override val stateCheck: StateMessage
) : MessageModel(id, role, timestamp, authorName, stateCheck) {
    override fun getLayout() : Int {
        return when(role) {
            USER -> R.layout.item_user_file_message
            OPERATOR -> R.layout.item_server_file_message
            NEUTRAL -> R.layout.item_default_message
        }
    }
}

data class UnionMessageItem(
    override val id: String,
    val idKey: Long,
    override val role: Role,
    val message: SpannableString,
    val actions: List<ActionItem>?,
    val file: FileModel,
    override val timestamp: Long,
    override val authorName: String,
    override val stateCheck: StateMessage
) : MessageModel(id, role, timestamp, authorName, stateCheck) {
    override fun getLayout() : Int {
        return when(role) {
            USER -> R.layout.item_user_union_message
            OPERATOR -> R.layout.item_server_union_message
            NEUTRAL -> R.layout.item_default_message
        }
    }
}