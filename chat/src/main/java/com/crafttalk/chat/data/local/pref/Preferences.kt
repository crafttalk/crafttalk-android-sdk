package com.crafttalk.chat.data.local.pref

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.crafttalk.chat.data.model.Visitor

fun checkVisitorInPref(pref: SharedPreferences): Boolean {
    return pref.getBoolean("isVisitor", false)
}

fun getVisitorFromPref(pref: SharedPreferences): Visitor {
    val gson = Gson()
    val json = pref.getString("Visitor", "")
    Log.d("PREFERENCES", "getVisitorFromPref - ${json}, obj = ${gson.fromJson<Visitor>(json, Visitor::class.java).toString()}")
    return gson.fromJson<Visitor>(json, Visitor::class.java)
}

@SuppressLint("CommitPrefEdits")
fun buildVisitorSaveToPref(mapWithDataUser: Map<String, Any>): Visitor? {
    return try {
        Visitor(
            generateUUID(),
            mapWithDataUser["firstName"] as String,
            mapWithDataUser["lastName"] as String,
            mapWithDataUser["email"] as? String,
            mapWithDataUser["phone"] as? String,
            mapWithDataUser["contract"] as? String,
            mapWithDataUser["birthday"] as? String
        )
    }
    catch (e: Exception) {
        null
    }
}

@SuppressLint("CommitPrefEdits")
fun saveVisitorToPref(pref: SharedPreferences, visitor: Visitor) {
    val prefEditor = pref.edit()
    prefEditor.putString("Visitor", visitor.getJsonObject().toString())
    prefEditor.putBoolean("isVisitor", true)
    prefEditor.apply()
}

fun deleteVisitorFromPref(pref: SharedPreferences) {
    val prefEditor = pref.edit()
    prefEditor.remove("Visitor")
    prefEditor.putBoolean("isVisitor", false)
    prefEditor.apply()
}

fun generateUUID(): String {
    return "stub53"
}