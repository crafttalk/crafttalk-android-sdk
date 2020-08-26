package com.crafttalk.chat.data.repository

import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.domain.entity.internet.TypeInternetConnection
import com.crafttalk.chat.domain.repository.IInternetConnectionRepository
import javax.inject.Inject

class InternetConnectionRepository
@Inject constructor(
    private val socketApi: SocketApi
): IInternetConnectionRepository {
    override fun setListener(changeInternetConnectionState: (TypeInternetConnection) -> Unit) {
        socketApi.setInternetConnectionListener(changeInternetConnectionState)
    }
}