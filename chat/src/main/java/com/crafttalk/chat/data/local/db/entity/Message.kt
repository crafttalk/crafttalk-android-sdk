package com.crafttalk.chat.data.local.db.entity

import androidx.room.*
import com.crafttalk.chat.data.remote.Action

@Entity(tableName = "messages")
data class Message(
    var id: String,
//    @ColumnInfo(name = "user_id")
//    val userId: Int,
//    @ColumnInfo(name = "client_id")
//    val clientId: Int,
    @ColumnInfo(name = "message_type")
    val messageType: Int,
    @ColumnInfo(name = "parent_msg_id")
    val parentMsgId: String?,
    val message: String?,
//    @TypeConverters(ActionConverter::class)
    val actions: Array<Action>?,
    @ColumnInfo(name = "is_reply")
    val isReply: Boolean,
//    @ColumnInfo(name = "id_from_channel")
//    val idFromChannel: String?,
    var timestamp: Long,
//    val score: Int?,
//    @ColumnInfo(name = "dialog_id")
//    val dialogId: String?,
    @ColumnInfo(name = "operator_name")
    val operatorName: String
//    @ColumnInfo(name = "from_history")
//    val fromHistory: Boolean
) {

    @PrimaryKey(autoGenerate = true)
    var idKey: Long = 0

}


