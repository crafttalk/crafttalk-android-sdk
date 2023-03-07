package com.crafttalk.chat.domain.entity.message

import com.crafttalk.chat.data.local.db.entity.WidgetEntity
import com.crafttalk.chat.utils.ChatParams
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class NetworkWidget(
    @SerializedName(value = "id", alternate = ["widgetId"])
    val widgetId: String,
    @SerializedName(value = "description")
    val description: String,
    @SerializedName(value = "params", alternate = ["payload"])
    val params: Any
) {

    companion object {

        fun map(widgetEntity: WidgetEntity) = NetworkWidget(
            widgetId = widgetEntity.widgetId,
            description = widgetEntity.description,
            params = widgetEntity.payload
        )
    }
}

class NetworkWidgetDeserializer : JsonDeserializer<NetworkWidget?> {

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): NetworkWidget? {
        val jsonObject = json?.asJsonObject ?: return null
        if (!jsonObject.has("id")) return null
        if (!jsonObject.has("params")) return null

        val widgetId = jsonObject["id"].asString
        val widgetDescription = if (jsonObject.has("description")) {
            jsonObject["description"].asString
        } else {
            ""
        }
        return try {
            val paramObj = Gson().fromJson(
                jsonObject["params"].toString(),
                ChatParams.methodGetPayloadTypeWidget(widgetId) ?: HashMap<String, Any>()::class.java
            )
            NetworkWidget(
                widgetId = widgetId,
                description = widgetDescription,
                params = paramObj
            )
        } catch (ex: Exception) {
            null
        }
    }
}