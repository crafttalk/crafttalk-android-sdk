package com.crafttalk.chat.data.local.db.entity

import android.util.Log
import androidx.room.ColumnInfo
import com.crafttalk.chat.domain.entity.message.NetworkAction
import kotlin.random.Random

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
            //https://youtrack.craft-talk.ru/issue/BCS_S-45
            //цикл нужен для проверки, что кнопка имеет actionId, в
            //ином случае при добавлении в локальную базу данных приложение вылетает
            for (element in actions){
                if (element.actionId == null) {
                    element.actionId = actions.indexOf(element).toString()
                }
            }
            return actions.map {
                ActionEntity(
                    it.actionId,
                    it.actionText,
                    false
                )
            }
        }
        fun map(actions: List<NetworkAction>, actionsSelected: List<String>): List<ActionEntity> {
            return actions.map {
                ActionEntity(
                    it.actionId,
                    it.actionText,
                    actionsSelected.contains(it.actionId)
                )
            }
        }
    }
}