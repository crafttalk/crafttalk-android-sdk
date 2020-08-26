package com.crafttalk.chat.domain.usecase.internet

import com.crafttalk.chat.domain.entity.internet.TypeInternetConnection
import com.crafttalk.chat.domain.repository.IInternetConnectionRepository
import javax.inject.Inject

class SetInternetConnectionListener
@Inject constructor(
    private val internetConnectionRepository: IInternetConnectionRepository
) {

    operator fun invoke(changeInternetConnectionState: (TypeInternetConnection) -> Unit) {
        internetConnectionRepository.setListener(changeInternetConnectionState)
    }

}