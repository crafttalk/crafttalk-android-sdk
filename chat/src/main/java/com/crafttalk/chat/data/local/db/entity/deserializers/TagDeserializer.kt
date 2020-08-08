package com.crafttalk.chat.data.local.db.entity.deserializers

import com.crafttalk.chat.domain.entity.tags.*
import com.google.gson.*
import java.lang.reflect.Type

class TagDeserializer(val gson: Gson): JsonDeserializer<List<Tag>> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): List<Tag> {
        val result = arrayListOf<Tag>()
        val jsonArray = json.asJsonArray
        jsonArray.forEach {
            val jsonObject = it.asJsonObject
            val name: String = jsonObject.get("name").asString
            val pointStart: Int = jsonObject.get("pointStart").asInt
            val pointEnd: Int = jsonObject.get("pointEnd").asInt
            when (name) {
                "strong" -> result.add(StrongTag(pointStart, pointEnd))
                "i" -> result.add(ItalicTag(pointStart, pointEnd))
                "a" -> result.add(UrlTag(pointStart, pointEnd, jsonObject.get("url").asString))
                "img" -> result.add(ImageTag(pointStart, pointEnd, jsonObject.get("url").asString, jsonObject.get("width").asInt, jsonObject.get("height").asInt))
                "li" -> result.add(ItemListTag(pointStart, pointEnd, jsonObject.get("countNesting").asInt))
                "ul" -> result.add(HostListTag(pointStart, pointEnd, jsonObject.get("countNesting").asInt))
            }
        }
        return result
    }
}