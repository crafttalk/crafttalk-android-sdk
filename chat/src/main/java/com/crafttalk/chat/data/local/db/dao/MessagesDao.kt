package com.crafttalk.chat.data.local.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.crafttalk.chat.data.local.db.entity.ActionEntity
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.file.TypeDownloadProgress

@Dao
interface MessagesDao {

    @Query("SELECT * FROM ${MessageEntity.TABLE_NAME} ORDER BY timestamp DESC")
    fun getMessages(): DataSource.Factory<Int, MessageEntity>

    @Query("SELECT EXISTS (SELECT * FROM ${MessageEntity.TABLE_NAME} LIMIT 1)")
    fun isNotEmpty(): Boolean

    @Query("SELECT COUNT(*) FROM ${MessageEntity.TABLE_NAME} WHERE timestamp > :currentReadMessageTime")
    fun getCountUnreadMessages(currentReadMessageTime: Long): Int?

    @Query("SELECT COUNT(*) FROM ${MessageEntity.TABLE_NAME} WHERE timestamp > :currentReadMessageTime AND timestamp <= :timestampLastMessage")
    fun getCountUnreadMessagesRange(currentReadMessageTime: Long, timestampLastMessage: Long): Int?

    @Query("SELECT timestamp FROM ${MessageEntity.TABLE_NAME} ORDER BY timestamp ASC LIMIT 1")
    fun getFirstTime(): Long?

    @Query("SELECT timestamp FROM ${MessageEntity.TABLE_NAME} ORDER BY timestamp DESC LIMIT 1")
    fun getLastTime(): Long?

    @Query("SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE id = :id")
    fun getMessageById(id: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessage(message: MessageEntity)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET message_type = :type WHERE id = :id")
    fun updateMessage(id: String, type: Int)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET height = :height, width = :width WHERE id = :id")
    fun updateSizeMessage(id: String, height: Int, width: Int)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET attachment_download_progress_type = :typeDownloadProgress WHERE id = :id")
    fun updateTypeDownloadProgress(id: String, typeDownloadProgress: TypeDownloadProgress)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET operator_name = :currentPersonName WHERE operator_id = :personId")
    fun updatePersonName(personId: String, currentPersonName: String)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET operator_preview = :newPersonPicture WHERE (operator_id = :personId) AND ((operator_preview != :newPersonPicture) OR (operator_preview is null AND :newPersonPicture is not null) OR (operator_preview is not null AND :newPersonPicture is null))")
    fun updatePersonPreview(personId: String, newPersonPicture: String?)

    @Query("DELETE FROM ${MessageEntity.TABLE_NAME}")
    fun deleteAllMessages()

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET actions = :actions WHERE id = :id")
    fun selectAction(id: String, actions: List<ActionEntity>?)

}