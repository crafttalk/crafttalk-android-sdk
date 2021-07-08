package com.crafttalk.chat.data.local.db.entity.converters

import androidx.room.TypeConverter
import com.crafttalk.chat.domain.entity.file.TypeFile

class TypeFileConverter {

    @TypeConverter
    fun fromTypeFile(typeFile: TypeFile?): String? {
        return typeFile?.name
    }

    @TypeConverter
    fun toTypeFile(typeFile: String?): TypeFile? {
        return typeFile?.let { TypeFile.valueOf(it) }
    }

}