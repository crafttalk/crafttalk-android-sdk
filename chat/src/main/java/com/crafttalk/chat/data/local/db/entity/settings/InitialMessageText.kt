package com.crafttalk.chat.data.local.db.entity.settings

import com.google.gson.annotations.SerializedName


data class InitialMessageText (

    @SerializedName("text"    ) var text    : String?            = null,
    @SerializedName("actions" ) var actions : ArrayList<Actions>? = arrayListOf()

)
