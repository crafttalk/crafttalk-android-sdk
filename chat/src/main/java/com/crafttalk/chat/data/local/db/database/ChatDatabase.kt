package com.crafttalk.chat.data.local.db.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.entity.converters.ActionConverter
import com.crafttalk.chat.data.local.db.entity.Message

@Database(
    entities = [Message::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ActionConverter::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessagesDao

    companion object {
        private var INSTANCE: ChatDatabase? = null

        private val lock = Any()

        fun getInstance(context: Context): ChatDatabase {
            if (INSTANCE == null) {
                synchronized(lock) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            ChatDatabase::class.java, "chat.db"
                        ).build()
                    }
                }
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}