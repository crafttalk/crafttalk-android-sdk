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
    Log.d("pref", "getVisitorFromPref - ${json}, obj = ${gson.fromJson<Visitor>(json, Visitor::class.java).toString()}")
    return gson.fromJson<Visitor>(json, Visitor::class.java)
}

@SuppressLint("CommitPrefEdits")
fun buildVisitorSaveToPref(pref: SharedPreferences, mapWithDataUser: Map<String, Any>): Visitor? {
    val prefEditor = pref.edit()
    try {
        val visitorObject = Visitor(
            generateUUID(),
            mapWithDataUser["firstName"] as String,
            mapWithDataUser["lastName"] as String,
            mapWithDataUser["email"] as? String,
            mapWithDataUser["phone"] as? String,
            mapWithDataUser["contract"] as? String,
            mapWithDataUser["birthday"] as? String
        )
//        val gson = Gson()
//        val json = gson.toJson(visitorObject)
//        Log.d("pref", "buildVisitorSaveToPref_1 - $json")
        Log.d("pref", "buildVisitorSaveToPref_2 - ${visitorObject.getJsonObject()}")
        Log.d("pref", "buildVisitorSaveToPref_put - ${visitorObject.getJsonObject().toString()}")
        prefEditor.putString("Visitor", visitorObject.getJsonObject().toString())
        prefEditor.putBoolean("isVisitor", true)
        prefEditor.apply()
        return visitorObject
    }
    catch (e: Exception) {
        prefEditor.putBoolean("isVisitor", false)
        return null
    }
}

fun generateUUID(): String {
    return "stub"
}