package com.crafttalk.chat.data.local.db.entity

import androidx.room.ColumnInfo
import com.crafttalk.chat.domain.entity.message.NetworkAction

data class ActionEntity(
    @ColumnInfo(name = "action_id")
    val actionId: String,
    @ColumnInfo(name = "action_text")
    val actionText: String,
    @ColumnInfo(name = "is_selected")
    val isSelected: Boolean
) {
    companion object {
        fun map(actions: List<NetworkAction>): List<ActionEntity> {
            return actions.map {
                ActionEntity(
                    it.actionId,
                    it.actionText,
                    false
                )
            }
        }
    }
}