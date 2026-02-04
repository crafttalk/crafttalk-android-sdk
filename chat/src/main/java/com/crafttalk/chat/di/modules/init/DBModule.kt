package com.crafttalk.chat.di.modules.init

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.crafttalk.chat.data.local.db.dao.FileDao
import com.crafttalk.chat.data.local.db.dao.MessagesDao
import com.crafttalk.chat.data.local.db.dao.PersonDao
import com.crafttalk.chat.data.local.db.database.ChatDatabase
import com.crafttalk.chat.data.local.db.migrations.*
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
    ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
        .addMigrations(
            Migration_1_2,
            Migration_2_3,
            Migration_3_4,
            Migration_4_5,
            Migration_5_6,
            Migration_6_7,
            Migration_7_10,
            Migration_9_10,
            Migration_10_11
        )
        /**
         * Из-за расхождения в версиях и неверной миграции между v7 - v8,
         * любая миграция на v8 или с v8, будет проходить через fallbackToDestructiveMigration
         */
        .fallbackToDestructiveMigrationFrom(8)
        .build()

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