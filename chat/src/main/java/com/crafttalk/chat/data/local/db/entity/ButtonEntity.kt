package com.crafttalk.chat.data.local.db.entity

import androidx.room.ColumnInfo
import com.crafttalk.chat.domain.entity.message.NetworkButton
import com.crafttalk.chat.domain.entity.message.NetworkButtonColor
import com.crafttalk.chat.domain.entity.message.NetworkButtonImage
import com.crafttalk.chat.domain.entity.message.NetworkButtonOperation

data class ButtonEntity(
    @ColumnInfo(name = "id")
    val buttonId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "action")
    val action: String,

    @ColumnInfo(name = "type")
    val typeOperation: NetworkButtonOperation,

    @ColumnInfo(name = "color")
    val color: NetworkButtonColor,

    @ColumnInfo(name = "image")
    val image: NetworkButtonImage? = null,

    @ColumnInfo(name = "imageEmoji")
    val imageEmoji: String? = null,

    @ColumnInfo(name = "fullSize")
    val hasFullSize: Boolean,

    @ColumnInfo(name = "selected")
    val selected: Boolean
) {

    companion object {
        fun map(networkButtons: List<NetworkButton>, buttonsSelected: List<String>): List<ButtonEntity> {
            return networkButtons.map {
                ButtonEntity(
                    buttonId = it.buttonId,
                    title = it.title,
                    action = it.action,
                    typeOperation = it.typeOperation,
                    color = it.color,
                    image = it.image,
                    imageEmoji = it.imageEmoji,
                    hasFullSize = it.hasFullSize,
                    selected = buttonsSelected.contains(it.buttonId)
                )
            }
        }
    }
}
