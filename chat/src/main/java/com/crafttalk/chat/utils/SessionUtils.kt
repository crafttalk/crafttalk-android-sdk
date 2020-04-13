package com.crafttalk.chat.utils

//import com.crafttalk.chat.utils.HashUtils.hashString

object SessionUtils {
    private const val apiKey = "xxx"
    const val signature = "xxx"
    val ts: String
        get() = (System.currentTimeMillis() / 1000).toString()

//    val hash: String
//        get() = hashString(
//            "SHA-224",
//            signature + ts + apiKey
//        )
}