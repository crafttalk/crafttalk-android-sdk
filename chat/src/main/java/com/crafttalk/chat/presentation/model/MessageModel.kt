package com.crafttalk.chat.presentation.model

import android.text.SpannableString
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseItem
import com.crafttalk.chat.domain.entity.message.MessageType as StateMessage

sealed class MessageModel(
    open val id: String,
    open val role: Role,
    open val timestamp: Long,
    open val authorName: String,
    open val stateCheck: StateMessage
) : BaseItem()

data class TextMessageItem(
    override val id: String,
    override val role: Role,
    override val timestamp: Long,
    val message: SpannableString,
    val actions: List<ActionItem>?,
    override val authorName: String,
    override val stateCheck: StateMessage
) : MessageModel(id, role, timestamp, authorName, stateCheck) {
    override fun getLayout(): Int {
        return when(role) {
            Role.USER -> R.layout.item_user_text_message
            Role.OPERATOR -> R.layout.item_server_text_message
        }
    }
    override fun <T : BaseItem> isSame(item: T): Boolean {
        return item is TextMessageItem && item.id == id
    }
}

data class ImageMessageItem(
    override val id: String,
    val idKey: Long,
    override val role: Role,
    val imageUrl: String,
    val imageName: String,
    override val timestamp: Long,
    override val authorName: String,
    override val stateCheck: StateMessage,
    val height: Int,
    val width: Int
) : MessageModel(id, role, timestamp, authorName, stateCheck) {
    override fun getLayout(): Int {
        return when(role) {
            Role.USER -> R.layout.item_user_image_message
            Role.OPERATOR -> R.layout.item_server_image_message
        }
    }
    override fun <T : BaseItem> isSame(item: T): Boolean {
        return item is ImageMessageItem && item.id == id
    }
}

data class GifMessageItem(
    override val id: String,
    val idKey: Long,
    override val role: Role,
    val gifUrl: String,
    val gifName: String,
    override val timestamp: Long,
    override val authorName: String,
    override val stateCheck: StateMessage,
    val height: Int,
    val width: Int
) : MessageModel(id, role, timestamp, authorName, stateCheck) {
    override fun getLayout(): Int {
        return when(role) {
            Role.USER -> R.layout.item_user_gif_message
            Role.OPERATOR -> R.layout.item_server_gif_message
        }
    }
    override fun <T : BaseItem> isSame(item: T): Boolean {
        return item is GifMessageItem && item.id == id
    }
}

data class FileMessageItem(
    override val id: String,
    override val role: Role,
    val fileUrl: String,
    val fileName: String,
    override val timestamp: Long,
    override val authorName: String,
    override val stateCheck: StateMessage
) : MessageModel(id, role, timestamp, authorName, stateCheck) {
    override fun getLayout() : Int {
        return when(role) {
            Role.USER -> R.layout.item_user_file_message
            Role.OPERATOR -> R.layout.item_server_file_message
        }
    }
    override fun <T : BaseItem> isSame(item: T): Boolean {
        return item is FileMessageItem && item.id == id
    }
}