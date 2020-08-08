package com.crafttalk.chat.domain.entity.message

import com.google.gson.annotations.SerializedName

data class Action (
    @SerializedName(value = "action_id")
    val actionId: String,
    @SerializedName (value = "action_text")
    val actionText: String
)