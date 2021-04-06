package com.crafttalk.chat.data.local.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.crafttalk.chat.data.local.db.entity.ActionEntity
import com.crafttalk.chat.data.local.db.entity.MessageEntity

@Dao
interface MessagesDao {

    @Query("SELECT * FROM messages WHERE uuid = :uuid ORDER BY timestamp DESC")
    fun getMessages(uuid: String): DataSource.Factory<Int, MessageEntity>

    @Query("SELECT timestamp FROM messages WHERE uuid = :uuid ORDER BY idKey DESC LIMIT 1")
    fun getLastTime(uuid: String): Long

    @Insert
    fun insertMessage(message: MessageEntity)

    @Insert
    fun insertMessages(messageList: List<MessageEntity>)

    @Query("UPDATE messages SET message_type = :type WHERE uuid = :uuid AND id = :id")
    fun updateMessage(uuid: String, id: String, type: Int)

    @Query("UPDATE messages SET height = :height, width = :width WHERE idKey = :idKey")
    fun updateSizeMessage(idKey: Long, height: Int, width: Int)

    @Query("SELECT * FROM messages WHERE uuid = :uuid AND id = :id")
    fun getMessageById(uuid: String, id: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE (uuid = :uuid) AND (message = :textMessage OR attachment_url = :attachmentUrl)")
    fun getMessageByContent(uuid: String, textMessage: String?, attachmentUrl: String?): List<MessageEntity>

    @Query("UPDATE messages SET is_read = 1 WHERE uuid = :uuid AND id = :id")
    fun readMessage(uuid: String, id: String)

    @Query("DELETE FROM messages WHERE uuid = :uuid")
    fun deleteAllMessages(uuid: String)

    @Query("UPDATE messages SET actions = :actions WHERE uuid = :uuid AND id = :id")
    fun selectAction(uuid: String, id: String, actions: List<ActionEntity>?)

}