package com.crafttalk.chat.domain.repository

interface INotificationRepository {
    suspend fun subscribe(uuid: String)
    suspend fun unSubscribe(uuid: String)
    suspend fun getToken(success: (token: String) -> Unit)
}