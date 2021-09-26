package com.crafttalk.chat.data.local.db.entity.converters

import androidx.room.TypeConverter
import com.crafttalk.chat.domain.entity.file.TypeDownloadProgress
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class TypeDownloadProgressConverter {

    @TypeConverter
    fun fromTypeFile(typeDownloadProgress: TypeDownloadProgress?): String? {
        typeDownloadProgress ?: return null
        val type: Type = object : TypeToken<TypeDownloadProgress>() {}.type
        val gson = Gson()
        return gson.toJson(typeDownloadProgress, type)
    }

    @TypeConverter
    fun toTypeFile(typeDownloadProgress: String?): TypeDownloadProgress? {
        typeDownloadProgress ?: return null
        val type: Type = object : TypeToken<TypeDownloadProgress>() {}.type
        val gson = Gson()
        return gson.fromJson(typeDownloadProgress, type)
    }

}