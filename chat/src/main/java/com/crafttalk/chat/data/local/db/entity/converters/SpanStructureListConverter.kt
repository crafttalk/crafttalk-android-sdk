package com.crafttalk.chat.data.local.db.entity.converters

import androidx.room.TypeConverter
import com.crafttalk.chat.data.local.db.entity.deserializers.TagDeserializer
import com.crafttalk.chat.domain.entity.tags.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.StringBuilder
import java.lang.reflect.Type

class SpanStructureListConverter {

    @TypeConverter
    fun fromSpanStructureList(tags: List<Tag>?): String {
        tags ?: return "[]"
        if (tags.isEmpty()) return "[]"

        val gson = Gson()

        val result = StringBuilder("[")
        tags.forEach {
            when (it) {
                is StrikeTag -> result.append(gson.toJson(it, StrikeTag::class.java)).append(",")
                is StrongTag -> result.append(gson.toJson(it, StrongTag::class.java)).append(",")
                is BTag -> result.append(gson.toJson(it, BTag::class.java)).append(",")
                is ItalicTag -> result.append(gson.toJson(it, ItalicTag::class.java)).append(",")
                is EmTag -> result.append(gson.toJson(it, EmTag::class.java)).append(",")
                is UrlTag -> result.append(gson.toJson(it, UrlTag::class.java)).append(",")
                is ImageTag -> result.append(gson.toJson(it, ImageTag::class.java)).append(",")
                is ItemListTag -> result.append(gson.toJson(it, ItemListTag::class.java)).append(",")
                is HostListTag -> result.append(gson.toJson(it, HostListTag::class.java)).append(",")
                is OrderedListTag -> result.append(gson.toJson(it, OrderedListTag::class.java)).append(",")
                is PhoneTag -> result.append(gson.toJson(it, PhoneTag::class.java)).append(",")
            }
        }
        result.replace(result.length - 1, result.length, "]")
        return result.toString()
    }

    @TypeConverter
    fun toSpanStructureList(tags: String?): List<Tag> {
        tags ?: return listOf()
        if (tags == "[]") return listOf()

        val type: Type = object : TypeToken<List<Tag>>() {}.type
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(type, TagDeserializer(Gson()))
        return gsonBuilder.create().fromJson(tags, type)
    }

}