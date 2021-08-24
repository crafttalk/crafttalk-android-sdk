package com.crafttalk.chat.data.local.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.crafttalk.chat.data.local.db.entity.MessageEntity

object Migration_1_2: Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL("CREATE TABLE ${MessageEntity.TABLE_NAME_BACKUP}(" +
                " uuid TEXT NOT NULL," +
                " id TEXT NOT NULL PRIMARY KEY," +
                " is_reply INTEGER NOT NULL," +
                " message_type INTEGER NOT NULL," +
                " parent_msg_id TEXT," +
                " timestamp INTEGER NOT NULL," +
                " message TEXT," +
                " span_structure_list TEXT NOT NULL," +
                " actions TEXT," +
                " attachment_url TEXT," +
                " attachment_type TEXT," +
                " attachment_name TEXT," +
                " attachment_size INTEGER," +
                " operator_id TEXT," +
                " operator_preview TEXT," +
                " operator_name TEXT," +
                " height INTEGER," +
                " width INTEGER" +
                ")")

        database.execSQL("INSERT INTO ${MessageEntity.TABLE_NAME_BACKUP} SELECT" +
                " uuid," +
                " id," +
                " is_reply," +
                " message_type," +
                " parent_msg_id," +
                " timestamp," +
                " message," +
                " spanStructureList," +
                " actions," +
                " attachment_url," +
                " attachment_type," +
                " attachment_name," +
                " attachment_size," +
                " operator_id," +
                " operator_preview," +
                " operator_name," +
                " height," +
                " width" +
                " FROM ${MessageEntity.TABLE_NAME}")

        database.execSQL("DROP TABLE ${MessageEntity.TABLE_NAME}")

        database.execSQL("CREATE TABLE ${MessageEntity.TABLE_NAME}(" +
                " uuid TEXT NOT NULL," +
                " id TEXT NOT NULL PRIMARY KEY," +
                " is_reply INTEGER NOT NULL," +
                " message_type INTEGER NOT NULL," +
                " parent_msg_id TEXT," +
                " timestamp INTEGER NOT NULL," +
                " message TEXT," +
                " span_structure_list TEXT NOT NULL," +
                " actions TEXT," +
                " attachment_url TEXT," +
                " attachment_type TEXT," +
                " attachment_name TEXT," +
                " attachment_size INTEGER," +
                " operator_id TEXT," +
                " operator_preview TEXT," +
                " operator_name TEXT," +
                " height INTEGER," +
                " width INTEGER" +
                ")")

        database.execSQL("INSERT INTO ${MessageEntity.TABLE_NAME} SELECT" +
                " uuid," +
                " id," +
                " is_reply," +
                " message_type," +
                " parent_msg_id," +
                " timestamp," +
                " message," +
                " span_structure_list," +
                " actions," +
                " attachment_url," +
                " attachment_type," +
                " attachment_name," +
                " attachment_size," +
                " operator_id," +
                " operator_preview," +
                " operator_name," +
                " height," +
                " width" +
                " FROM ${MessageEntity.TABLE_NAME_BACKUP}")

        database.execSQL("DROP TABLE ${MessageEntity.TABLE_NAME_BACKUP}")

    }
}