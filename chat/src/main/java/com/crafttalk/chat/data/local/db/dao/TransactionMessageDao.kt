package com.crafttalk.chat.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.crafttalk.chat.data.local.db.entity.MessageEntity

@Dao
abstract class TransactionMessageDao {

    @Query("SELECT NOT EXISTS (SELECT * FROM messages WHERE uuid = :uuid AND id = :id)")
    abstract fun isNotExistMessage(uuid: String, id: String): Boolean

    @Insert
    abstract fun insert(message: MessageEntity)

    @Transaction
    open fun insertMessage(message: MessageEntity) {
        if (isNotExistMessage(message.uuid, message.id)) {
            insert(message)
        }
    }

}