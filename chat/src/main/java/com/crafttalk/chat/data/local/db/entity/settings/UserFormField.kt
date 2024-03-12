package com.crafttalk.chat.data.local.db.entity.settings

import com.google.gson.annotations.SerializedName

/**
 * Расширение для класса SettingFromServer
 */
data class UserFormField(
    @SerializedName(value = "fieldName")
    val fieldName: String,
    @SerializedName (value = "mask")
    val mask: String,
    @SerializedName(value = "maxLength")
    val maxLength: String,
    @SerializedName(value = "validationErrorMessage")
    val validationErrorMessage: String,
    @SerializedName(value = "validationRegex")
    val validationRegex: String,
    @SerializedName(value = "visitorField")
    val visitorField: String
)
