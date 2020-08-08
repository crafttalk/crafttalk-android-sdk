package com.crafttalk.chat.data.local.pref

import android.content.SharedPreferences
import android.util.Log
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.google.gson.Gson

fun checkVisitorInPref(pref: SharedPreferences): Boolean {
    return pref.getBoolean("isVisitor", false)
}

fun getVisitorFromPref(pref: SharedPreferences): Visitor {
    val gson = Gson()
    val json = pref.getString("Visitor", "")
    Log.d(
        "PREFERENCES",
        "getVisitorFromPref - ${json}, obj = ${gson.fromJson<Visitor>(json, Visitor::class.java)}"
    )
    val visitor = gson.fromJson<Visitor>(json, Visitor::class.java)
    Uuid.lastUUID = visitor.uuid
    return visitor
}


object Uuid {

    var lastUUID: String = ""

    fun generateUUID(isFirstAuth: Boolean, vararg loginParts: String): String {
        return if (isFirstAuth) {
            var resultUUID = ""
            for (part in loginParts) {
                resultUUID = "${resultUUID}${part}"
            }
            lastUUID = resultUUID
            resultUUID
        }
        else {
            lastUUID
        }
    }

}

