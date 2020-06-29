package com.crafttalk.chat.data.remote.pojo

import com.google.gson.annotations.SerializedName

data class Message (
    val id: String,
    @SerializedName (value = "message_type")
    val messageType: Int,
    val isReply : Boolean,
    @SerializedName (value = "parent_message_id", alternate = arrayOf("parent_msg_id"))
    val parentMessageId: String?,
    val timestamp: Long,
    val message: String?,
    val actions: Array<Action>?,
    @SerializedName (value = "attachment_url")
    val attachmentUrl: String?,
    @SerializedName (value = "attachment_type")
    val attachmentType: String?,
    @SerializedName (value = "attachment_name")
    val attachmentName: String?,
    @SerializedName (value = "operator_name")
    val operatorName: String?,
    @SerializedName (value = "reply_to_message")
    val replyToMessage: Message?
)





