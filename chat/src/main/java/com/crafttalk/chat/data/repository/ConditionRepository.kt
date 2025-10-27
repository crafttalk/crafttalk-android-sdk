package com.crafttalk.chat.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.crafttalk.chat.data.api.socket.SocketApi
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.domain.repository.IConditionRepository
import com.crafttalk.chat.initialization.ChatMessageListener
import com.crafttalk.chat.presentation.ChatInternetConnectionListener
import com.crafttalk.chat.utils.ChatStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ConditionRepository @Inject constructor(
    private val messagesDao: MessagesDao,
    private val pref: DataStore<Preferences>,
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

    /**
     * Получение флага "вся история загружена"
     * runBlocking используется для синхронного вызова (если нужно в UI — лучше сделать suspend)
     */
    override fun getFlagAllHistoryLoaded(): Boolean = runBlocking {
        val prefs = pref.data.first()
        return@runBlocking prefs[IS_ALL_HISTORY_LOADED_KEY] ?: false
    }

    override fun saveFlagAllHistoryLoaded(isAllHistoryLoaded: Boolean): Unit = runBlocking {
        pref.edit { prefs ->
            prefs[IS_ALL_HISTORY_LOADED_KEY] = isAllHistoryLoaded
        }
    }

    override fun deleteFlagAllHistoryLoaded():Unit = runBlocking {
        pref.edit { prefs ->
            prefs.remove(IS_ALL_HISTORY_LOADED_KEY)
        }
    }

    override fun getCurrentReadMessageTime(): Long = runBlocking {
        val prefs = pref.data.first()
        return@runBlocking prefs[CURRENT_READ_MESSAGE_TIME_KEY] ?: 0L
    }

    override fun getCountUnreadMessages(): Int = runBlocking{
        val prefs = pref.data.first()
        return@runBlocking prefs[COUNT_UNREAD_MESSAGES_KEY] ?: 0
    }

    override fun saveCurrentReadMessageTime(currentReadMessageTime: Long):Unit = runBlocking {
        pref.edit { prefs ->
            prefs[CURRENT_READ_MESSAGE_TIME_KEY] = currentReadMessageTime
        }
    }

    override fun saveCountUnreadMessages(countUnreadMessages: Int):Unit = runBlocking {
        pref.edit { prefs ->
            prefs[COUNT_UNREAD_MESSAGES_KEY] = countUnreadMessages
        }
    }

    override fun deleteCurrentReadMessageTime():Unit = runBlocking {
        pref.edit { prefs ->
            prefs.remove(CURRENT_READ_MESSAGE_TIME_KEY)
        }
    }

    override suspend fun getStatusExistenceMessages(): Boolean {
        return messagesDao.isNotEmpty()
    }

    override fun deleteAllMessage() {
        messagesDao.deleteAllMessages()
    }

    companion object {
        private val IS_ALL_HISTORY_LOADED_KEY = booleanPreferencesKey("isAllHistoryLoaded")
        private val CURRENT_READ_MESSAGE_TIME_KEY = longPreferencesKey("currentReadMessageTime")
        private val COUNT_UNREAD_MESSAGES_KEY = intPreferencesKey("countUnreadMessages")
    }
}