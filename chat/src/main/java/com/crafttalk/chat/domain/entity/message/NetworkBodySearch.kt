package com.crafttalk.chat.domain.entity.message

import com.crafttalk.chat.utils.ChatParams

data class NetworkBodySearch(
    val visitorUuid: String,
    val searchText: String,
    val clientId: String = ChatParams.urlChatNameSpace!!,
    val searchFields: String = "All",
    val lastMessageTimestamp: Long = 1660662206650
)