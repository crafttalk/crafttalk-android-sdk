package com.crafttalk.chat.domain.entity.message

import com.google.gson.annotations.SerializedName

data class NetworkButtonImage(

    @SerializedName("url")
    val url: String,

    @SerializedName("align")
    val align: NetworkButtonImageAlign

)