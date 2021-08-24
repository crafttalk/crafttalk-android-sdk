package com.crafttalk.chat.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.crafttalk.chat.domain.interactors.*

@Suppress("UNCHECKED_CAST")
class ChatViewModelFactory constructor(
    private val authChatInteractor: AuthInteractor,
    private val messageInteractor: MessageInteractor,
    private val fileInteractor: FileInteractor,
    private val conditionInteractor: ConditionInteractor,
    private val feedbackInteractor: FeedbackInteractor,
    private val context: Context
): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChatViewModel(
            authChatInteractor,
            messageInteractor,
            fileInteractor,
            conditionInteractor,
            feedbackInteractor,
            context
        ) as T
    }
}