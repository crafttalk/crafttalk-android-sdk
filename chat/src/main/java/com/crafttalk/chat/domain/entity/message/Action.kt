package com.crafttalk.chat.data.remote.pojo

import com.google.gson.annotations.SerializedName

data class Action (
    @SerializedName(value = "action_id")
    val actionId: String,
    @SerializedName (value = "action_text")
    val actionText: String
)