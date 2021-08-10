package com.crafttalk.chat.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.crafttalk.chat.domain.interactors.*

@Suppress("UNCHECKED_CAST")
class ChatViewModelFactory constructor(
    private val authChatInteractor: AuthInteractor,
    private val chatMessageInteractor: ChatMessageInteractor,
    private val fileInteractor: FileInteractor,
    private val conditionInteractor: ConditionInteractor,
    private val context: Context
): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChatViewModel(
            authChatInteractor,
            chatMessageInteractor,
            fileInteractor,
            conditionInteractor,
            context
        ) as T
    }
}