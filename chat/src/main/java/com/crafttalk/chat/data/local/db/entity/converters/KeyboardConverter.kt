package com.crafttalk.chat.data.local.db.entity.converters

import androidx.room.TypeConverter
import com.crafttalk.chat.data.local.db.entity.KeyboardEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class KeyboardConverter {

    @TypeConverter
    fun fromActions(keyboard: KeyboardEntity?): String? {
        keyboard ?: return null
        val type: Type = object : TypeToken<KeyboardEntity>() {}.type
        val gson = Gson()
        return gson.toJson(keyboard, type)
    }

    @TypeConverter
    fun toActions(keyboard: String?): KeyboardEntity? {
        keyboard ?: return null
        val type: Type = object : TypeToken<KeyboardEntity>() {}.type
        val gson = Gson()
        return gson.fromJson(keyboard, type)
    }

}