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

    @Query("SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace ORDER BY timestamp DESC, arrival_time DESC")
    fun getAllMessages(namespace: String): List<MessageEntity>

    @Query("SELECT EXISTS (SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace LIMIT 1)")
    fun isNotEmpty(namespace: String): Boolean

    @Query("SELECT COUNT(*) FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace AND timestamp > :timestamp")
    fun getPositionByTimestamp(namespace: String, timestamp: Long): Int?

    @Query("SELECT EXISTS (SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace AND id = :id)")
    fun emptyAvailable(namespace: String, id: String): Boolean

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

    @Query("SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace AND id = :id")
    fun getMessageById(namespace: String, id: String): MessageEntity?

    @Query("SELECT timestamp FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace AND id = :id")
    fun getTimestampMessageById(namespace: String, id: String): Long?

    @Query("SELECT EXISTS (SELECT * FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace AND id = :id LIMIT 1)")
    fun hasThisMessage(namespace: String, id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessages(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessage(message: MessageEntity)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET message_type = :type WHERE namespace = :namespace AND id = :id")
    fun updateMessage(namespace: String, id: String, type: Int)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET height = :height, width = :width WHERE namespace = :namespace AND id = :id")
    fun updateSizeMessage(namespace: String, id: String, height: Int, width: Int)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET attachment_download_progress_type = :typeDownloadProgress WHERE namespace = :namespace AND id = :id")
    fun updateTypeDownloadProgress(namespace: String, id: String, typeDownloadProgress: TypeDownloadProgress)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET operator_name = :currentPersonName WHERE namespace = :namespace AND operator_id = :personId")
    fun updatePersonName(namespace: String, personId: String, currentPersonName: String)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET operator_preview = :newPersonPicture WHERE (namespace = :namespace) AND (operator_id = :personId) AND ((operator_preview != :newPersonPicture) OR (operator_preview is null AND :newPersonPicture is not null) OR (operator_preview is not null AND :newPersonPicture is null))")
    fun updatePersonPreview(namespace: String, personId: String, newPersonPicture: String?)

    @Query("DELETE FROM ${MessageEntity.TABLE_NAME}")
    fun deleteAllMessages()

    @Query("DELETE FROM ${MessageEntity.TABLE_NAME} WHERE namespace = :namespace AND message_type = :messageType")
    fun deleteAllMessageByType(namespace: String, messageType: Int)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET actions = :actions WHERE namespace = :namespace AND id = :id")
    fun selectAction(namespace: String, id: String, actions: List<ActionEntity>?)

    @Query("UPDATE ${MessageEntity.TABLE_NAME} SET keyboard = :keyboard WHERE namespace = :namespace AND id = :id")
    fun selectButton(namespace: String, id: String, keyboard: KeyboardEntity?)

}