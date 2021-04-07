package com.crafttalk.chat.di.modules.init

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.crafttalk.chat.data.local.db.dao.FileDao
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.dao.PersonDao
import com.crafttalk.chat.data.local.db.database.ChatDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DBModule {

    @Provides
    @Singleton
    fun provideChatDatabase(
        context: Context
    ): ChatDatabase = Room.databaseBuilder(
        context,
        ChatDatabase::class.java,
        "chat.db"
    ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE).build()

    @Provides
    @Singleton
    fun provideMessagesDao(
        chatDatabase: ChatDatabase
    ): MessagesDao = chatDatabase.messageDao()

    @Provides
    @Singleton
    fun providePersonDao(
        chatDatabase: ChatDatabase
    ): PersonDao = chatDatabase.personDao()

    @Provides
    @Singleton
    fun provideFileDao(
        chatDatabase: ChatDatabase
    ): FileDao = chatDatabase.fileDao()

}