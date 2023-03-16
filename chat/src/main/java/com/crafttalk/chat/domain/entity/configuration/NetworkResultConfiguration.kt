package com.crafttalk.chat.domain.entity.configuration

import java.io.Serializable

data class NetworkResultConfiguration(
    val chatAnnouncement: String,
    val showInitialMessage: Boolean,
    val sendInitialMessageOnOpen: Boolean = true,
    val sendInitialMessageOnStartDialog: Boolean = false
): Serializable