package com.crafttalk.chat.domain.repository

import com.crafttalk.chat.domain.entity.internet.TypeInternetConnection

interface IInternetConnectionRepository {
    fun setListener(changeInternetConnectionState: (TypeInternetConnection) -> Unit)
}