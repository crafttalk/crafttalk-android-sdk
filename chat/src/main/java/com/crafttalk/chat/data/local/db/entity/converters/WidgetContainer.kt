package com.crafttalk.chat.data.local.db.entity.converters

import androidx.room.TypeConverter
import com.crafttalk.chat.data.local.db.entity.WidgetEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class WidgetContainer {

    @TypeConverter
    fun fromWidget(widget: WidgetEntity?): String? {
        widget ?: return null
        val type: Type = object : TypeToken<WidgetEntity>() {}.type
        val gson = Gson()
        return gson.toJson(widget, type)
    }

    @TypeConverter
    fun toWidget(widget: String?): WidgetEntity? {
        widget ?: return null
        val type: Type = object : TypeToken<WidgetEntity>() {}.type
        val gson = Gson()
        return gson.fromJson(widget, type)
    }
}