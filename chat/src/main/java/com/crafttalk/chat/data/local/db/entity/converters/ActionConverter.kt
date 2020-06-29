package com.crafttalk.chat.data.local.db.entity.converters

import androidx.room.TypeConverter
import com.crafttalk.chat.data.remote.pojo.Action

class ActionConverter {

    @TypeConverter
    fun fromActions(actions: Array<Action>?): String? {
        return when {
            actions == null -> null
            actions.isEmpty() -> ""
            else -> {
                val resultStringBuffer = StringBuffer()
                actions.forEach {
                    resultStringBuffer.append(it.actionId).append("~").append(it.actionText).append(";")
                }
                resultStringBuffer.deleteCharAt(resultStringBuffer.length - 1)
                resultStringBuffer.toString()
            }
        }
    }

    @TypeConverter
    fun toActions(actions: String?): Array<Action>? {
        return when {
            actions == null -> null
            actions.isEmpty() -> arrayOf()
            else -> {
                val resultList = arrayListOf<Action>()
                var actionPart: List<String>
                val nodes = actions.split(";")
                nodes.forEach {
                    actionPart = it.split("~")
                    resultList.add(
                        Action(
                            actionPart[0],
                            actionPart[1]
                        )
                    )
                }
                resultList.toTypedArray()
            }
        }
    }

}