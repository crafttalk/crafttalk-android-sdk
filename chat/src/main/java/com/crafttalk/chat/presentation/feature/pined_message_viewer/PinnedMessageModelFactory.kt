package com.crafttalk.chat.presentation.feature.pined_message_viewer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.crafttalk.chat.domain.interactors.AuthInteractor
import com.crafttalk.chat.domain.interactors.ConditionInteractor
import com.crafttalk.chat.domain.interactors.ConfigurationInteractor
import com.crafttalk.chat.domain.interactors.FeedbackInteractor
import com.crafttalk.chat.domain.interactors.FileInteractor
import com.crafttalk.chat.domain.interactors.MessageInteractor
import com.crafttalk.chat.domain.interactors.SearchInteractor


class PinnedMessageModelFactory (
    private val authChatInteractor: AuthInteractor,
    private val messageInteractor: MessageInteractor,
    private val searchInteractor: SearchInteractor,
    private val fileInteractor: FileInteractor,
    private val conditionInteractor: ConditionInteractor,
    private val feedbackInteractor: FeedbackInteractor,
    private val configurationInteractor: ConfigurationInteractor,
    private val context: Context
):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PinnedMessageModel(
            authChatInteractor,
            messageInteractor,
            searchInteractor,
            fileInteractor,
            conditionInteractor,
            feedbackInteractor,
            configurationInteractor,
            context
        ) as T
    }

}