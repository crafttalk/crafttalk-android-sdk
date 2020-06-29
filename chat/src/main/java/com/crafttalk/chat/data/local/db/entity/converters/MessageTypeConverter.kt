package com.crafttalk.chat.data.local.db.entity.converters

import androidx.room.TypeConverter
import com.crafttalk.chat.data.model.MessageType
import com.crafttalk.chat.data.model.MessageType.Companion.getMessageTypeByValueType

class MessageTypeConverter {

    @TypeConverter
    fun fromMessageType(messageType: MessageType): Int {
        return messageType.valueType
    }

    @TypeConverter
    fun toMessageType(messageType: Int): MessageType {
        return getMessageTypeByValueType(messageType)
    }

}