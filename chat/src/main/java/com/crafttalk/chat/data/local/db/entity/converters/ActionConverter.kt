package com.crafttalk.chat.data.local.db.entity.converters

import androidx.room.TypeConverter
import com.crafttalk.chat.domain.entity.message.Action
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class ActionConverter {

    @TypeConverter
    fun fromActions(actions: List<Action>?): String? {
        actions ?: return null
        val type: Type = object : TypeToken<List<Action>>() {}.type
        val gson = Gson()
        return gson.toJson(actions, type)
    }

    @TypeConverter
    fun toActions(actions: String?): List<Action>? {
        actions ?: return null
        val type: Type = object : TypeToken<List<Action>>() {}.type
        val gson = Gson()
        return gson.fromJson(actions, type)
    }

}