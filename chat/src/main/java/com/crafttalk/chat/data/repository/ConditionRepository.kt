package com.crafttalk.chat.data.repository

import android.content.SharedPreferences
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.domain.repository.IConditionRepository
import com.crafttalk.chat.initialization.ChatMessageListener
import com.crafttalk.chat.presentation.ChatInternetConnectionListener
import com.crafttalk.chat.utils.ChatStatus
import javax.inject.Inject
import android.util.Log

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
            socketApi.resetNewMessagesCounter()
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
        return pref.getBoolean(FIELD_IS_ALL_HISTORY_LOADED, false).apply {
            Log.d("TEST_LOG_HISTORY", "getFlagAllHistoryLoaded isAllHistoryLoaded: ${this};")
        }
    }

    override fun saveFlagAllHistoryLoaded(isAllHistoryLoaded: Boolean) {
        Log.d("TEST_LOG_HISTORY", "saveFlagAllHistoryLoaded isAllHistoryLoaded: ${isAllHistoryLoaded};")
        val prefEditor = pref.edit()
        prefEditor.putBoolean(FIELD_IS_ALL_HISTORY_LOADED, isAllHistoryLoaded)
        prefEditor.apply()
    }

    override fun deleteFlagAllHistoryLoaded() {
        val prefEditor = pref.edit()
        prefEditor.remove(FIELD_IS_ALL_HISTORY_LOADED)
        prefEditor.apply()
    }

    override fun getCurrentReadMessageTime(): Long {
        return pref.getLong(FIELD_CURRENT_READ_MESSAGE_TIME, 0)
    }

    override fun getCountUnreadMessages(): Int {
        return pref.getInt(FIELD_COUNT_UNREAD_MESSAGES, 0)
    }

    override fun saveCurrentReadMessageTime(currentReadMessageTime: Long) {
        val prefEditor = pref.edit()
        prefEditor.putLong(FIELD_CURRENT_READ_MESSAGE_TIME, currentReadMessageTime)
        prefEditor.apply()
    }

    override fun saveCountUnreadMessages(countUnreadMessages: Int) {
        val prefEditor = pref.edit()
        prefEditor.putInt(FIELD_COUNT_UNREAD_MESSAGES, countUnreadMessages)
        prefEditor.apply()
    }

    override fun deleteCurrentReadMessageTime() {
        val prefEditor = pref.edit()
        prefEditor.remove(FIELD_CURRENT_READ_MESSAGE_TIME)
        prefEditor.apply()
    }

    override suspend fun getStatusExistenceMessages(): Boolean {
        return messagesDao.isNotEmpty()
    }

    companion object {
        private const val FIELD_IS_ALL_HISTORY_LOADED = "isAllHistoryLoaded"
        private const val FIELD_CURRENT_READ_MESSAGE_TIME = "currentReadMessageTime"
        private const val FIELD_COUNT_UNREAD_MESSAGES = "countUnreadMessages"
    }

}