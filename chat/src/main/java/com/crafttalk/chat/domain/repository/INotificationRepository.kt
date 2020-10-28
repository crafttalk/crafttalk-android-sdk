package com.crafttalk.chat.domain.repository

interface INotificationRepository {
     fun subscribe(uuid: String)
     fun unSubscribe(uuid: String)
     fun getToken(success: (token: String) -> Unit)
}