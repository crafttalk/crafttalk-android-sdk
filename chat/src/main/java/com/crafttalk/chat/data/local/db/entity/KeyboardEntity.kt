package com.crafttalk.chat.data.local.db.entity

import androidx.room.ColumnInfo
import com.crafttalk.chat.domain.entity.message.NetworkKeyboard

data class KeyboardEntity(
    @ColumnInfo(name = "buttons")
    val buttons: List<List<ButtonEntity>>
) {

    companion object {

        fun map(keyboard: NetworkKeyboard, buttonsSelected: List<String>): KeyboardEntity {
            val buttons: MutableList<List<ButtonEntity>> = mutableListOf()
            keyboard.buttons.forEach { horizontalButtons ->
                buttons.add(ButtonEntity.map(horizontalButtons, buttonsSelected))
            }
            return KeyboardEntity(
                buttons = buttons
            )
        }
    }
}