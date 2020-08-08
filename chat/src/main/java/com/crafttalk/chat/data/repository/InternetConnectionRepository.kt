package com.crafttalk.chat.data.repository

import com.crafttalk.chat.data.remote.socket_service.SocketApi
import com.crafttalk.chat.domain.entity.internet.TypeInternetConnection
import com.crafttalk.chat.domain.repository.IInternetConnectionRepository

class InternetConnectionRepository(
    private val socketApi: SocketApi
): IInternetConnectionRepository {
    override fun setListener(changeInternetConnectionState: (TypeInternetConnection) -> Unit) {
        socketApi.setInternetConnectionListener(changeInternetConnectionState)
    }
}