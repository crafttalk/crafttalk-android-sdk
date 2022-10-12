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
            if (!jsonObject.has("name")) return@forEach
            if (!jsonObject.has("pointStart")) return@forEach
            if (!jsonObject.has("pointEnd")) return@forEach
            val name: String = jsonObject.get("name").asString
            val pointStart: Int = jsonObject.get("pointStart").asInt
            val pointEnd: Int = jsonObject.get("pointEnd").asInt
            when (name) {
                "strike" -> result.add(StrikeTag(pointStart, pointEnd))
                "strong" -> result.add(StrongTag(pointStart, pointEnd))
                "b" -> result.add(BTag(pointStart, pointEnd))
                "i" -> result.add(ItalicTag(pointStart, pointEnd))
                "em" -> result.add(EmTag(pointStart, pointEnd))
                "a" -> {
                    if (!jsonObject.has("url")) return@forEach
                    result.add(UrlTag(pointStart, pointEnd, jsonObject.get("url").asString))
                }
                "img" -> {
                    if (!jsonObject.has("url")) return@forEach
                    if (!jsonObject.has("width")) return@forEach
                    if (!jsonObject.has("height")) return@forEach
                    result.add(ImageTag(pointStart, pointEnd, jsonObject.get("url").asString, jsonObject.get("width").asInt, jsonObject.get("height").asInt))
                }
                "li" -> {
                    if (!jsonObject.has("countNesting")) return@forEach
                    result.add(ItemListTag(pointStart, pointEnd, jsonObject.get("countNesting").asInt))
                }
                "ul" -> {
                    if (!jsonObject.has("countNesting")) return@forEach
                    result.add(HostListTag(pointStart, pointEnd, jsonObject.get("countNesting").asInt))
                }
                "phone" -> {
                    if (!jsonObject.has("phone")) return@forEach
                    result.add(PhoneTag(pointStart, pointEnd, jsonObject.get("phone").asString))
                }
            }
        }
        return result
    }
}