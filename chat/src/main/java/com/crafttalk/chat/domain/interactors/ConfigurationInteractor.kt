package com.crafttalk.chat.domain.interactors

import com.crafttalk.chat.domain.repository.IConfigurationRepository
import com.crafttalk.chat.utils.ChatParams
import javax.inject.Inject

class ConfigurationInteractor
@Inject constructor(
    private val configurationRepository: IConfigurationRepository
) {

    fun getConfiguration() {
        val config = configurationRepository.getConfiguration() ?: return
        ChatParams.glueMessage = config.chatAnnouncement
        ChatParams.sendInitialMessageOnOpen = true//config.sendInitialMessageOnOpen
        ChatParams.sendInitialMessageOnStartDialog = false//config.sendInitialMessageOnStartDialog
        ChatParams.showInitialMessage = config.showInitialMessage
    }

}