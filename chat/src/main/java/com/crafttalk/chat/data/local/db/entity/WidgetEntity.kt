package com.crafttalk.chat.data.local.db.entity

import androidx.room.ColumnInfo
import com.crafttalk.chat.domain.entity.message.NetworkWidget

data class WidgetEntity(
    @ColumnInfo(name = "id")
    val widgetId: String,
    @ColumnInfo(name = "payload")
    val payload: Map<String, Any>
) {

    companion object {

        fun map(networkWidget: NetworkWidget): WidgetEntity {
            return WidgetEntity(
                widgetId = networkWidget.widgetId,
                payload = networkWidget.params
            )
        }
    }
}