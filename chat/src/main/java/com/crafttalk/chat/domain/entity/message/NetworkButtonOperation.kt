package com.crafttalk.chat.domain.entity.message

import com.google.gson.annotations.SerializedName

enum class NetworkButtonOperation {

    @SerializedName("Url")
    URL,

    @SerializedName("Action")
    ACTION

}