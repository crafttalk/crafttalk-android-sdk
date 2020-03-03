package com.crafttalk.chat.data.local.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.crafttalk.chat.data.local.db.entity.Message

@Dao
interface MessagesDao {
    @Query("SELECT * FROM messages")
    fun getMessages(): LiveData<List<Message>>

    @Insert
    fun insertMessage(message: Message)

    @Insert
    fun insertMessages(messageList: List<Message>)

    @Query("SELECT * FROM messages ORDER BY id DESC LIMIT :limit")
    fun getLastMessages(limit: Int): List<Message>

    @Query("UPDATE messages SET id = :newId WHERE idKey = :idKey")
    fun updateMessage(idKey: Long, newId: String)
}