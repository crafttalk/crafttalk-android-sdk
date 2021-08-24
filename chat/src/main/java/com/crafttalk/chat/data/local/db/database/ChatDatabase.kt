package com.crafttalk.chat.data.local.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crafttalk.chat.data.local.db.dao.FileDao
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.dao.PersonDao
import com.crafttalk.chat.data.local.db.entity.FileEntity
import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.data.local.db.entity.PersonEntity
import com.crafttalk.chat.data.local.db.entity.converters.ActionConverter
import com.crafttalk.chat.data.local.db.entity.converters.SpanStructureListConverter
import com.crafttalk.chat.data.local.db.entity.converters.TypeFileConverter

@Database(
    entities = [MessageEntity::class, PersonEntity::class, FileEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(ActionConverter::class, TypeFileConverter::class, SpanStructureListConverter::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun messageDao(): MessagesDao
    abstract fun personDao(): PersonDao
    abstract fun fileDao(): FileDao
}