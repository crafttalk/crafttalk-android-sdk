package com.crafttalk.chat.data.local.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.crafttalk.chat.data.local.db.entity.Message

@Dao
interface MessagesDao {

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getMessages(): DataSource.Factory<Int, Message>

    @Query("SELECT timestamp FROM messages ORDER BY idKey DESC LIMIT 1")
    fun getLastTime(): Long

    @Insert
    fun insertMessage(message: Message)

    @Insert
    fun insertMessages(messageList: List<Message>)

    @Query("UPDATE messages SET message_type = :type WHERE id = :id")
    fun updateMessage(id: String, type: Int)

    @Query("UPDATE messages SET height = :height, width = :width WHERE idKey = :idKey")
    fun updateSizeMessage(idKey: Long, height: Int, width: Int)

    @Query("SELECT * FROM messages WHERE id = :id")
    fun getMessageById(id: String): Message?

    @Query("SELECT * FROM messages WHERE message = :textMessage OR attachment_url = :attachmentUrl")
    fun getMessageByContent(textMessage: String?, attachmentUrl: String?): List<Message>

}