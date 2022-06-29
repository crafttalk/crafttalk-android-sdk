package com.crafttalk.chat.data.local.db.entity

import androidx.room.ColumnInfo
import com.crafttalk.chat.domain.entity.message.NetworkWidget

data class WidgetEntity(
    @ColumnInfo(name = "id")
    val widgetId: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "payload")
    val payload: Any
) {

    companion object {

        fun map(networkWidget: NetworkWidget): WidgetEntity {
            return WidgetEntity(
                widgetId = networkWidget.widgetId,
                description = networkWidget.description,
                payload = networkWidget.params
            )
        }
    }
}