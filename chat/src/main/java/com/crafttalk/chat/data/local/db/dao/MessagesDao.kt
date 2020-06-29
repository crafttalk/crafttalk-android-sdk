package com.crafttalk.chat.data.local.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.crafttalk.chat.data.local.db.entity.Message

@Dao
interface MessagesDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getMessagesLiveData(): LiveData<List<Message>>

    @Query("SELECT * FROM messages")
    fun getMessagesList(): List<Message>

    @Query("SELECT timestamp FROM messages ORDER BY idKey DESC LIMIT 1")
    fun getLastTime(): Long

    @Insert
    fun insertMessage(message: Message)

    @Insert
    fun insertMessages(messageList: List<Message>)

    @Query("UPDATE messages SET message_type = :type WHERE id = :id")
    fun updateMessage(id: String, type: Int)

    @Query("SELECT * FROM messages WHERE id = :id")
    fun getMessageById(id: String): Message?
}