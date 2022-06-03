package com.crafttalk.chat.domain.entity.message

import com.crafttalk.chat.data.local.db.entity.WidgetEntity
import com.google.gson.annotations.SerializedName

data class NetworkWidget(
    @SerializedName(value = "id")
    val widgetId: String,
    @SerializedName(value = "params")
    val params: Map<String, Any>
) {

    companion object {

        fun map(widgetEntity: WidgetEntity) = NetworkWidget(
            widgetId = widgetEntity.widgetId,
            params = widgetEntity.payload
        )
    }
}