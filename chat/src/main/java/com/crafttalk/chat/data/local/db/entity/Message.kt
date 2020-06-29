package com.crafttalk.chat.data.local.db.entity

import android.util.Log
import androidx.room.*
import com.crafttalk.chat.data.remote.pojo.Action
import kotlin.math.abs

@Entity(tableName = "messages")
data class Message(
    var id: String,
    @ColumnInfo(name = "is_reply")
    val isReply: Boolean,
    @ColumnInfo(name = "message_type")
    val messageType: Int,
    @ColumnInfo(name = "parent_msg_id")
    val parentMsgId: String?,
    var timestamp: Long,
    val message: String?,
    val actions: Array<Action>?,
    @ColumnInfo(name = "attachment_url")
    val attachmentUrl: String?,
    @ColumnInfo(name = "attachment_type")
    val attachmentType: String?,
    @ColumnInfo(name = "attachment_name")
    val attachmentName: String?,
    @ColumnInfo(name = "operator_name")
    val operatorName: String?
) {
    @PrimaryKey(autoGenerate = true)
    var idKey: Long = 0

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Message -> {
                        this.isReply == other.isReply &&
                        this.parentMsgId == other.parentMsgId &&
                        this.message == other.message &&
                        (this.actions.isNullOrEmpty() && other.actions.isNullOrEmpty()) || (!this.actions.isNullOrEmpty() && !other.actions.isNullOrEmpty() && this.actions.contentEquals(other.actions)) &&
                        this.attachmentUrl == other.attachmentUrl &&
                        this.attachmentType == other.attachmentType &&
                        this.attachmentName == other.attachmentName &&
                        this.operatorName == other.operatorName &&
                        abs(this.timestamp - other.timestamp) <= 50

            }
            else -> false
        }
    }


}


