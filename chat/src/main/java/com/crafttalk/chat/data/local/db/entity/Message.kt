package com.crafttalk.chat.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.crafttalk.chat.data.helper.converters.text.convertTextToNormalString
import com.crafttalk.chat.domain.entity.message.Action
import com.crafttalk.chat.domain.entity.tags.Tag
import kotlin.math.abs
import com.crafttalk.chat.domain.entity.message.Message as MessageSocket

@Entity(tableName = "messages")
data class Message(
    val uuid: String,
    var id: String,
    @ColumnInfo(name = "is_reply")
    val isReply: Boolean,
    @ColumnInfo(name = "message_type")
    val messageType: Int,
    @ColumnInfo(name = "parent_msg_id")
    val parentMsgId: String?,
    var timestamp: Long,
    val message: String?,
//    @ColumnInfo(name = "span_structure_list")
    val spanStructureList: List<Tag>,
    val actions: List<Action>?,
    @ColumnInfo(name = "attachment_url")
    val attachmentUrl: String?,
    @ColumnInfo(name = "attachment_type")
    val attachmentType: String?,
    @ColumnInfo(name = "attachment_name")
    val attachmentName: String?,
    @ColumnInfo(name = "operator_preview")
    val operatorPreview: String?,
    @ColumnInfo(name = "operator_name")
    val operatorName: String?,
    @ColumnInfo(name = "height")
    val height: Int?,
    @ColumnInfo(name = "width")
    val width: Int?,
    @ColumnInfo(name = "is_read")
    val isRead: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    var idKey: Long = 0

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Message -> {
                        this.uuid == other.uuid &&
                        this.isReply == other.isReply &&
                        this.parentMsgId == other.parentMsgId &&
                        this.message == other.message &&
                        (this.actions.isNullOrEmpty() && other.actions.isNullOrEmpty()) || (!this.actions.isNullOrEmpty() && !other.actions.isNullOrEmpty() && this.actions == other.actions) &&
                        this.attachmentUrl == other.attachmentUrl &&
                        this.attachmentType == other.attachmentType &&
                        this.attachmentName == other.attachmentName &&
                        this.operatorName == other.operatorName &&
                        abs(this.timestamp - other.timestamp) <= 50
            }
            else -> false
        }
    }

    companion object {
        fun map(uuid: String, messageSocket: MessageSocket, operatorPreview: String?, height: Int? = null, width: Int? = null): Message {
            val list = arrayListOf<Tag>()
            val message = messageSocket.message?.convertTextToNormalString(list)

            return Message(
                uuid = uuid,
                id = messageSocket.id,
                messageType = messageSocket.messageType,
                isReply = messageSocket.isReply,
                parentMsgId = messageSocket.parentMessageId,
                timestamp = messageSocket.timestamp,
                message = message,
                spanStructureList = list,
                actions = messageSocket.actions,
                attachmentUrl = messageSocket.attachmentUrl,
                attachmentType = messageSocket.attachmentType,
                attachmentName = messageSocket.attachmentName,
                operatorPreview = operatorPreview,
                operatorName = if (messageSocket.operatorName == null || !messageSocket.isReply) "Вы" else messageSocket.operatorName,
                height = height,
                width = width,
                isRead = false
            )
        }
    }

}