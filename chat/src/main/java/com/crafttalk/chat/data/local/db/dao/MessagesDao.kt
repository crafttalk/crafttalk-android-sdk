package com.crafttalk.chat.data.local.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.crafttalk.chat.data.local.db.entity.ActionEntity
import com.crafttalk.chat.data.local.db.entity.MessageEntity

@Dao
interface MessagesDao {

    @Query("SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE uuid = :uuid ORDER BY timestamp DESC")
    fun getMessages(uuid: String): DataSource.Factory<Int, MessageEntity>

    @Query("SELECT EXISTS (SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE uuid = :uuid LIMIT 1)")
    fun isNotEmpty(uuid: String): Boolean

    @Query("SELECT timestamp FROM ${MessageEntity.TABLE_NAME} WHERE uuid = :uuid ORDER BY timestamp ASC LIMIT 1")
    fun getFirstTime(uuid: String): Long?

    @Query("SELECT timestamp FROM ${MessageEntity.TABLE_NAME} WHERE uuid = :uuid ORDER BY timestamp DESC LIMIT 1")
    fun getLastTime(uuid: String): Long?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessage(message: MessageEntity)


    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET message_type = :type WHERE uuid = :uuid AND id = :id")
    fun updateMessage(uuid: String, id: String, type: Int)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET height = :height, width = :width WHERE uuid = :uuid AND id = :id")
    fun updateSizeMessage(uuid: String, id: String, height: Int, width: Int)

    @Query("SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE uuid = :uuid AND id = :id")
    fun getMessageById(uuid: String, id: String): MessageEntity?

    @Query("SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE (uuid = :uuid) AND (message = :textMessage OR attachment_url = :attachmentUrl)")
    fun getMessageByContent(uuid: String, textMessage: String?, attachmentUrl: String?): List<MessageEntity>

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET operator_name = :currentPersonName WHERE operator_id = :personId")
    fun updatePersonName(personId: String, currentPersonName: String)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET operator_preview = :newPersonPicture WHERE (operator_id = :personId) AND ((operator_preview != :newPersonPicture) OR (operator_preview is null AND :newPersonPicture is not null) OR (operator_preview is not null AND :newPersonPicture is null))")
    fun updatePersonPreview(personId: String, newPersonPicture: String?)

    @Query("DELETE FROM ${MessageEntity.TABLE_NAME} WHERE uuid = :uuid")
    fun deleteAllMessages(uuid: String)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET actions = :actions WHERE uuid = :uuid AND id = :id")
    fun selectAction(uuid: String, id: String, actions: List<ActionEntity>?)

}