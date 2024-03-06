package com.crafttalk.chat.data.local.db.entity.settings

import com.google.gson.annotations.SerializedName

/**
 * Расширение для класса SettingFromServer
 */
data class WebchatVersion(
    @SerializedName(value = "branch")
    val branch: String? = null,
    @SerializedName(value = "build_date")
    val build_date: String? = null,
    @SerializedName(value = "commit")
    val commit: String? = null
)
