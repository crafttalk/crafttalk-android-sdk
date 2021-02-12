package com.crafttalk.chat.domain.entity.message

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Message (
    val id: String,
    @SerializedName (value = "message_type")
    val messageType: Int,
    val isReply : Boolean,
    @SerializedName (value = "parent_message_id", alternate = arrayOf("parent_msg_id"))
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
) : Serializable