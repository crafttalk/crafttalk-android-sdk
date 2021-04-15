package com.crafttalk.chat.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.crafttalk.chat.data.helper.converters.text.convertTextToNormalString
import com.crafttalk.chat.domain.entity.tags.Tag
import kotlin.math.abs
import com.crafttalk.chat.domain.entity.message.Message as MessageSocket

@Entity(tableName = "messages")
data class MessageEntity(
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
    val actions: List<ActionEntity>?,
    @ColumnInfo(name = "attachment_url")
    val attachmentUrl: String?,
    @ColumnInfo(name = "attachment_type")
    val attachmentType: String?,
    @ColumnInfo(name = "attachment_name")
    val attachmentName: String?,
    @ColumnInfo(name = "operator_id")
    val operatorId: String?,
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

    fun hasSelectedAction(): Boolean {
        return if (actions == null) {
            false
        } else {
            actions.find { it.isSelected } != null
        }
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is MessageEntity -> {
                        this.uuid == other.uuid &&
                        this.isReply == other.isReply &&
                        this.parentMsgId == other.parentMsgId &&
                        this.message == other.message &&
//                        (this.actions.isNullOrEmpty() && other.actions.isNullOrEmpty()) || (!this.actions.isNullOrEmpty() && !other.actions.isNullOrEmpty() && this.actions == other.actions) &&
                        this.attachmentUrl == other.attachmentUrl &&
                        this.attachmentType == other.attachmentType &&
                        this.attachmentName == other.attachmentName &&
                        this.operatorName == other.operatorName &&
                        abs(this.timestamp - other.timestamp) <= 50
            }
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + isReply.hashCode()
        result = 31 * result + (parentMsgId?.hashCode() ?: 0)
        result = 31 * result + (message?.hashCode() ?: 0)
//        result = if (actions.isNullOrEmpty()) 31 * result else 31 * result + (actions.hashCode())
        result = 31 * result + (attachmentUrl?.hashCode() ?: 0)
        result = 31 * result + (attachmentType?.hashCode() ?: 0)
        result = 31 * result + (attachmentName?.hashCode() ?: 0)
        result = 31 * result + (operatorName?.hashCode() ?: 0)
        return result
    }

    companion object {
        fun map(uuid: String, messageSocket: MessageSocket, operatorPreview: String?, height: Int? = null, width: Int? = null): MessageEntity {
            val list = arrayListOf<Tag>()
            val message = messageSocket.message?.convertTextToNormalString(list)

            return MessageEntity(
                uuid = uuid,
                id = messageSocket.id,
                messageType = messageSocket.messageType,
                isReply = messageSocket.isReply,
                parentMsgId = messageSocket.parentMessageId,
                timestamp = messageSocket.timestamp,
                message = message,
                spanStructureList = list,
                actions = messageSocket.actions?.let { ActionEntity.map(it) },
                attachmentUrl = messageSocket.attachmentUrl,
                attachmentType = messageSocket.attachmentType,
                attachmentName = messageSocket.attachmentName,
                operatorId = messageSocket.operatorId,
                operatorPreview = operatorPreview,
                operatorName = if (messageSocket.isReply) messageSocket.operatorName else "Вы",
                height = height,
                width = width,
                isRead = false
            )
        }
    }

}