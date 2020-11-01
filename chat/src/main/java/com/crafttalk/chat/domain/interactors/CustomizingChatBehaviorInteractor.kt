package com.crafttalk.chat.domain.interactors

import com.crafttalk.chat.domain.repository.IChatBehaviorRepository
import com.crafttalk.chat.initialization.ChatInternetConnectionListener
import com.crafttalk.chat.initialization.ChatMessageListener
import javax.inject.Inject

class CustomizingChatBehaviorInteractor
@Inject constructor(
    private val сhatBehaviorRepository: IChatBehaviorRepository
) {

    fun setInternetConnectionListener(listener: ChatInternetConnectionListener) {
        сhatBehaviorRepository.setInternetConnectionListener(listener)
    }

    fun setMessageListener(listener: ChatMessageListener) {
        сhatBehaviorRepository.setMessageListener(listener)
    }

}