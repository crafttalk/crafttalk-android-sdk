package com.crafttalk.chat.domain.entity.message

import com.crafttalk.chat.data.local.db.entity.KeyboardEntity
import com.google.gson.annotations.SerializedName

data class NetworkKeyboard (

    @SerializedName(value = "buttons")
    val buttons: List<List<NetworkButton>>

) {

    companion object {

        fun map(keyboardEntity: KeyboardEntity): NetworkKeyboard {
            val buttons: MutableList<List<NetworkButton>> = mutableListOf()
            keyboardEntity.buttons.forEach { horizontalButtons ->
                buttons.add(horizontalButtons.map { NetworkButton.map(it) })
            }
            return NetworkKeyboard(
                buttons = buttons
            )
        }
    }
}