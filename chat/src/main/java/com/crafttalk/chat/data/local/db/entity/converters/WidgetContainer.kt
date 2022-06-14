package com.crafttalk.chat.data.local.db.entity.converters

import androidx.room.TypeConverter
import com.crafttalk.chat.data.local.db.entity.WidgetEntity
import com.crafttalk.chat.utils.ChatParams
import com.google.gson.Gson
import org.json.JSONObject

class WidgetContainer {

    @TypeConverter
    fun fromWidget(widget: WidgetEntity?): String? {
        widget ?: return null
        return Gson().toJson(widget, WidgetEntity::class.java)
    }

    @TypeConverter
    fun toWidget(widget: String?): WidgetEntity? {
        widget ?: return null
        val jsonObject = JSONObject(widget)
        val widgetId = jsonObject["widgetId"].toString()
        return try {
            val payloadObj = Gson().fromJson(
                jsonObject["payload"].toString(),
                ChatParams.methodGetPayloadTypeWidget(widgetId) ?: HashMap<String, Any>()::class.java
            )
            WidgetEntity(
                widgetId = widgetId,
                payload = payloadObj
            )
        } catch (ex: Exception) {
            null
        }
    }
}