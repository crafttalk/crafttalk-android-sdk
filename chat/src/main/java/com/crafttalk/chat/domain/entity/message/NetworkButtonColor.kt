package com.crafttalk.chat.domain.entity.message

import com.google.gson.annotations.SerializedName

enum class NetworkButtonColor {

    @SerializedName("Primary")
    PRIMARY,

    @SerializedName("Secondary")
    SECONDARY,

    @SerializedName("Negative")
    NEGATIVE

}