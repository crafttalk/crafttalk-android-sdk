package com.crafttalk.chat.domain.entity.message

import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.utils.ChatAttr
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Message (
    var id: String?,
    @SerializedName (value = "message_type")
    val messageType: Int,
    val isReply : Boolean,
    @SerializedName (value = "parent_message_id", alternate = ["parent_msg_id"])
    val parentMessageId: String?,
    val timestamp: Long,
    var message: String?,
    val actions: List<Action>?,
    @SerializedName (value = "attachment_url")
    var attachmentUrl: String?,
    @SerializedName (value = "attachment_type")
    val attachmentType: String?,
    @SerializedName (value = "attachment_name")
    val attachmentName: String?,
    @SerializedName (value = "operator_id")
    val operatorId: String?,
    @SerializedName (value = "operator_name")
    val operatorName: String?,
    @SerializedName (value = "reply_to_message")
    val replyToMessage: Message?
) : Serializable {

    val isImage = !attachmentUrl.isNullOrEmpty() &&
            !attachmentName.isNullOrEmpty() &&
            !attachmentType.isNullOrEmpty() &&
            (attachmentType == "IMAGE" || attachmentType.toLowerCase(
                ChatAttr.getInstance().locale).startsWith("image"))

    val isGif = !attachmentUrl.isNullOrEmpty() &&
            !attachmentName.isNullOrEmpty() &&
            !attachmentType.isNullOrEmpty() &&
            (attachmentType == "IMAGE" || attachmentType.toLowerCase(
                ChatAttr.getInstance().locale).startsWith("image")) &&
            attachmentName.contains(".GIF", true)

    val isFile = !attachmentUrl.isNullOrEmpty() &&
            !attachmentName.isNullOrEmpty() &&
            !attachmentType.isNullOrEmpty() &&
            attachmentType == "FILE"

    val attachmentTypeFile = when {
        isFile -> TypeFile.FILE
        isImage -> TypeFile.IMAGE
        isGif -> TypeFile.GIF
        else -> null
    }

}