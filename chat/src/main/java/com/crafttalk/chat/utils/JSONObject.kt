package com.crafttalk.chat.utils

import org.json.JSONException
import org.json.JSONObject

fun JSONObject.getOrNull(key: String): Any? {
    return if (has(key)) {
        try {
            get(key)
        } catch (ex: JSONException) {
            null
        }
    } else {
        null
    }
}

fun JSONObject.getStringOrNull(key: String): String? {
    return if (has(key)) {
        try {
            getString(key)
        } catch (ex: JSONException) {
            null
        }
    } else {
        null
    }
}