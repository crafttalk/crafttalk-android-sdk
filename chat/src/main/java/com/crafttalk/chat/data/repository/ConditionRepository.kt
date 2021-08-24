package com.crafttalk.chat.data.repository

import android.content.SharedPreferences
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.domain.repository.IConditionRepository
import com.crafttalk.chat.initialization.ChatMessageListener
import com.crafttalk.chat.presentation.ChatInternetConnectionListener
import com.crafttalk.chat.utils.ChatStatus
import javax.inject.Inject

class ConditionRepository
@Inject constructor(
    private val messagesDao: MessagesDao,
    private val pref: SharedPreferences,
    private val socketApi: SocketApi
) : IConditionRepository {

    override fun setInternetConnectionListener(listener: ChatInternetConnectionListener) {
        socketApi.setInternetConnectionListener(listener)
    }

    override fun setMessageListener(listener: ChatMessageListener) {
        socketApi.setMessageListener(listener)
    }

    override fun setStatusChat(newStatus: ChatStatus) {
        if (newStatus in listOf(ChatStatus.ON_CHAT_SCREEN_FOREGROUND_APP, ChatStatus.ON_CHAT_SCREEN_BACKGROUND_APP)) {
            socketApi.cleanBufferMessages()
        }
        socketApi.chatStatus = newStatus
    }

    override fun getStatusChat(): ChatStatus = socketApi.chatStatus

    override fun createSessionChat() {
        socketApi.initSocket()
    }

    override fun destroySessionChat() {
        socketApi.destroySocket()
    }

    override fun dropChat() {
        socketApi.dropChat()
    }

    override fun getFlagAllHistoryLoaded(): Boolean {
        return pref.getBoolean("isAllHistoryLoaded", false)
    }

    override fun saveFlagAllHistoryLoaded(isAllHistoryLoaded: Boolean) {
        val prefEditor = pref.edit()
        prefEditor.putBoolean("isAllHistoryLoaded", isAllHistoryLoaded)
        prefEditor.apply()
    }

    override fun getCurrentReadMessageTime(): Long {
        return pref.getLong("currentReadMessageTime", 0)
    }

    override fun saveCurrentReadMessageTime(currentReadMessageTime: Long) {
        val prefEditor = pref.edit()
        prefEditor.putLong("currentReadMessageTime", currentReadMessageTime)
        prefEditor.apply()
    }

    override suspend fun getStatusExistenceMessages(uuid: String): Boolean {
        return messagesDao.isNotEmpty(uuid)
    }

}