package com.crafttalk.chat.data.local.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.crafttalk.chat.data.local.db.entity.ActionEntity
import com.crafttalk.chat.data.local.db.entity.KeyboardEntity
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.file.TypeDownloadProgress

@Dao
interface MessagesDao {

    @Query("SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace ORDER BY timestamp DESC, arrival_time DESC")
    fun getMessages(namespace: String): DataSource.Factory<Int, MessageEntity>

    @Query("SELECT EXISTS (SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace LIMIT 1)")
    fun isNotEmpty(namespace: String): Boolean

    @Query("SELECT COUNT(*) FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace AND timestamp > :timestamp")
    fun getPositionByTimestamp(namespace: String, timestamp: Long): Int?

    @Query("SELECT EXISTS (SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE id = :id)")
    fun emptyAvailable(id: String): Boolean

    @Query("SELECT COUNT(*) FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace AND timestamp > :currentReadMessageTime AND message_type NOT IN (:ignoredMessageTypes)")
    fun getCountUnreadMessages(namespace: String, currentReadMessageTime: Long, ignoredMessageTypes: List<Int>): Int?

    @Query("SELECT COUNT(*) FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace AND timestamp <= :timestampMessage")
    fun getCountMessagesInclusiveTimestamp(namespace: String, timestampMessage: Long): Int?

    @Query("SELECT COUNT(*) FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace AND timestamp > :currentReadMessageTime AND timestamp <= :timestampLastMessage AND message_type NOT IN (:ignoredMessageTypes)")
    fun getCountUnreadMessagesRange(namespace: String, currentReadMessageTime: Long, timestampLastMessage: Long, ignoredMessageTypes: List<Int>): Int?

    @Query("SELECT timestamp FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace ORDER BY timestamp ASC LIMIT 1")
    fun getFirstTime(namespace: String): Long?

    @Query("SELECT timestamp FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace ORDER BY timestamp DESC LIMIT 1")
    fun getLastTime(namespace: String): Long?

    @Query("SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE id = :id")
    fun getMessageById(id: String): MessageEntity?

    @Query("SELECT timestamp FROM ${MessageEntity.TABLE_NAME} WHERE id = :id")
    fun getTimestampMessageById(id: String): Long?

    @Query("SELECT EXISTS (SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE id = :id LIMIT 1)")
    fun hasThisMessage(id: String): Boolean

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

    @Query("DELETE FROM ${MessageEntity.TABLE_NAME} WHERE message_type = :messageType")
    fun deleteAllMessageByType(messageType: Int)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET actions = :actions WHERE id = :id")
    fun selectAction(id: String, actions: List<ActionEntity>?)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET keyboard = :keyboard WHERE id = :id")
    fun selectButton(id: String, keyboard: KeyboardEntity?)

}