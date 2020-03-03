package com.crafttalk.chat.data.remote

class Message (
    val id: String?,
    val message_type: Int,
    val isReply : Boolean,
    val parent_message_id: String?,
    val timestamp: Long,
    val message: String?,
    val actions: Array<Action>?,
    val attachment_url: String?,
    val attachment_type: String?,
    val attachment_name: String?,
    val operator_name: String?,
    val reply_to_message: Message?
)





