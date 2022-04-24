package com.crafttalk.chat.domain.entity.message

import com.crafttalk.chat.data.local.db.entity.ButtonEntity
import com.google.gson.annotations.SerializedName

data class NetworkButton (

    @SerializedName(value = "id")
    val buttonId: String,

    @SerializedName(value = "title")
    val title: String,

    @SerializedName(value = "action")
    val action: String,

    @SerializedName(value = "type")
    val typeOperation: NetworkButtonOperation,

    @SerializedName(value = "color")
    val color: NetworkButtonColor,

    @SerializedName(value = "image")
    val image: NetworkButtonImage? = null,

    @SerializedName(value = "imageEmoji")
    val imageEmoji: String? = null,

    @SerializedName(value = "fullSize")
    val hasFullSize: Boolean

) {

    companion object {

        fun map(buttonEntity: ButtonEntity) = NetworkButton(
            buttonId = buttonEntity.buttonId,
            title = buttonEntity.title,
            action = buttonEntity.action,
            typeOperation = buttonEntity.typeOperation,
            color = buttonEntity.color,
            image = buttonEntity.image,
            imageEmoji = buttonEntity.imageEmoji,
            hasFullSize = buttonEntity.hasFullSize
        )
    }
}