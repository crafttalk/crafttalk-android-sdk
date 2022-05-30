package com.crafttalk.chat.data.helper.network

import retrofit2.Call
import java.io.IOException

fun <T> Call<T>.toData(): T? {
    return try {
        val response = execute()
        if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    } catch (e: IOException) {
        null
    }
}