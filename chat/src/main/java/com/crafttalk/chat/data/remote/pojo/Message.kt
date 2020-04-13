package com.crafttalk.chat.data.remote.pojo

import com.google.gson.annotations.SerializedName

data class Message (
    val id: String,
    val message_type: Int,
    val isReply : Boolean,
    @SerializedName (value = "parent_message_id", alternate = arrayOf("parent_msg_id"))
    val parentMessageId: String,
    val timestamp: Long,
    val message: String?,
    val actions: Array<Action>?,
    val attachment_url: String?,
    val attachment_type: String?,
    val attachment_name: String?,
    val operator_name: String?,
    val reply_to_message: Message?
)





