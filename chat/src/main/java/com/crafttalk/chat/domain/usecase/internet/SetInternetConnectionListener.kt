package com.crafttalk.chat.domain.usecase.internet

import com.crafttalk.chat.domain.entity.internet.TypeInternetConnection
import com.crafttalk.chat.domain.repository.IInternetConnectionRepository

class SetInternetConnectionListener constructor(
    private val internetConnectionRepository: IInternetConnectionRepository
) {

    operator fun invoke(changeInternetConnectionState: (TypeInternetConnection) -> Unit) {
        internetConnectionRepository.setListener(changeInternetConnectionState)
    }

}