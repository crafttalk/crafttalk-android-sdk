package com.crafttalk.chat.data.helper.network

import retrofit2.Call

fun <T> Call<T>.toData(): T? {
    val response = execute()
    if (response.isSuccessful) {
        return response.body()
    } else {
        throw Exception(response.errorBody()?.string())
    }
}