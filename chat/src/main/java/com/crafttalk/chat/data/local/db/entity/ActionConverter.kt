package com.crafttalk.chat.data.local.db.entity

import android.util.Log
import androidx.room.TypeConverter
import com.crafttalk.chat.data.remote.Action

class ActionConverter {

    @TypeConverter
    fun fromActions(actions: Array<Action>?): String? {
        return if (actions == null){
            null
        } else if (actions.isEmpty()) {
            ""
        }
        else {
            Log.d("CONVERTER","action = ${actions.toString()}")
            val resultStringBuffer = StringBuffer()
            actions.forEach {
                Log.d("CONVERTER","action it = ${it}, ${it.action_text}")
                resultStringBuffer.append(it.action_id).append("~").append(it.action_text).append(";")
            }
            resultStringBuffer.deleteCharAt(resultStringBuffer.length - 1)
            resultStringBuffer.toString()
        }
    }

    @TypeConverter
    fun toActions(actions: String?): Array<Action>? {
        return if (actions == null){
            null
        } else if (actions.isEmpty()) {
            arrayOf()
        }
        else {
            val resultList = arrayListOf<Action>()
            var actionPart: List<String>
            val nodes = actions.split(";")
            nodes.forEach {
                actionPart = it.split("~")
                resultList.add(Action(actionPart[0], actionPart[1]))
            }
            resultList.toTypedArray()
        }
    }

}