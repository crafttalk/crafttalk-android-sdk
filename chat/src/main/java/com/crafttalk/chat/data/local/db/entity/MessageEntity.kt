package com.crafttalk.chat.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.crafttalk.chat.data.helper.converters.text.convertTextToNormalString
import com.crafttalk.chat.data.local.db.entity.MessageEntity.Companion.TABLE_NAME
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.tags.Tag
import kotlin.math.abs
import com.crafttalk.chat.domain.entity.message.NetworkMessage

@Entity(tableName = TABLE_NAME)
data class MessageEntity(

    @ColumnInfo(name = "uuid")
    val uuid: String,

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: String,

    @ColumnInfo(name = "is_reply")
    val isReply: Boolean,

    @ColumnInfo(name = "message_type")
    val messageType: Int,

    @ColumnInfo(name = "parent_msg_id")
    val parentMsgId: String?,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "message")
    val message: String?,

    @ColumnInfo(name = "span_structure_list")
    val spanStructureList: List<Tag>,

    @ColumnInfo(name = "actions")
    val actions: List<ActionEntity>?,

    @ColumnInfo(name = "attachment_url")
    val attachmentUrl: String?,

    @ColumnInfo(name = "attachment_type")
    val attachmentType: TypeFile?,

    @ColumnInfo(name = "attachment_name")
    val attachmentName: String?,

    @ColumnInfo(name = "attachment_size")
    val attachmentSize: Long?,

    @ColumnInfo(name = "operator_id")
    val operatorId: String?,

    @ColumnInfo(name = "operator_preview")
    val operatorPreview: String?,

    @ColumnInfo(name = "operator_name")
    val operatorName: String?,

    @ColumnInfo(name = "height")
    val height: Int?,

    @ColumnInfo(name = "width")
    val width: Int?

) {

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
                        abs(this.timestamp - other.timestamp) <= COUNT_MS
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
        private const val COUNT_MS = 1000
        const val TABLE_NAME = "messages"
        const val TABLE_NAME_BACKUP = "messages_backup"

        fun map(
            uuid: String,
            networkMessage: NetworkMessage,
            operatorPreview: String?,
            fileSize: Long? = null,
            mediaFileHeight: Int? = null,
            mediaFileWidth: Int? = null
        ): MessageEntity {
            val list = arrayListOf<Tag>()
            val message = networkMessage.message?.convertTextToNormalString(list)

            return MessageEntity(
                uuid = uuid,
                id = networkMessage.id!!,
                messageType = networkMessage.messageType,
                isReply = networkMessage.isReply,
                parentMsgId = networkMessage.parentMessageId,
                timestamp = networkMessage.timestamp,
                message = message,
                spanStructureList = list,
                actions = networkMessage.actions?.let { ActionEntity.map(it) },
                attachmentUrl = networkMessage.attachmentUrl,
                attachmentType = networkMessage.attachmentTypeFile,
                attachmentName = networkMessage.attachmentName,
                attachmentSize = fileSize,
                operatorId = networkMessage.operatorId,
                operatorPreview = operatorPreview,
                operatorName = if (networkMessage.isReply) networkMessage.operatorName else "Вы",
                height = mediaFileHeight,
                width = mediaFileWidth
            )
        }

        fun mapOperatorMessage(
            uuid: String,
            networkMessage: NetworkMessage,
            actionsSelected: List<String>,
            operatorPreview: String?,
            fileSize: Long? = null,
            mediaFileHeight: Int? = null,
            mediaFileWidth: Int? = null
        ): MessageEntity {
            val list = arrayListOf<Tag>()
            val message = networkMessage.message?.convertTextToNormalString(list)

            return MessageEntity(
                uuid = uuid,
                id = networkMessage.id!!,
                messageType = networkMessage.messageType,
                isReply = true,
                parentMsgId = networkMessage.parentMessageId,
                timestamp = networkMessage.timestamp,
                message = message,
                spanStructureList = list,
                actions = networkMessage.actions?.let { ActionEntity.map(it, actionsSelected) },
                attachmentUrl = networkMessage.attachmentUrl,
                attachmentType = networkMessage.attachmentTypeFile,
                attachmentName = networkMessage.attachmentName,
                attachmentSize = fileSize,
                operatorId = networkMessage.operatorId,
                operatorPreview = operatorPreview,
                operatorName = networkMessage.operatorName,
                height = mediaFileHeight,
                width = mediaFileWidth
            )
        }

        fun mapUserMessage(
            uuid: String,
            networkMessage: NetworkMessage,
            status: Int,
            operatorPreview: String?,
            fileSize: Long? = null,
            mediaFileHeight: Int? = null,
            mediaFileWidth: Int? = null
        ): MessageEntity {
            val list = arrayListOf<Tag>()
            val message = networkMessage.message?.convertTextToNormalString(list)

            return MessageEntity(
                uuid = uuid,
                id = networkMessage.idFromChannel!!,
                messageType = status,
                isReply = false,
                parentMsgId = networkMessage.parentMessageId,
                timestamp = networkMessage.timestamp,
                message = message,
                spanStructureList = list,
                actions = null,
                attachmentUrl = networkMessage.attachmentUrl,
                attachmentType = networkMessage.attachmentTypeFile,
                attachmentName = networkMessage.attachmentName,
                attachmentSize = fileSize,
                operatorId = networkMessage.operatorId,
                operatorPreview = operatorPreview,
                operatorName = "Вы",
                height = mediaFileHeight,
                width = mediaFileWidth
            )
        }

        fun mapOperatorJoinMessage(
            uuid: String,
            networkMessage: NetworkMessage,
            operatorPreview: String?
        ): MessageEntity {
            return MessageEntity(
                uuid = uuid,
                id = networkMessage.id!!,
                messageType = networkMessage.messageType,
                isReply = true,
                parentMsgId = networkMessage.parentMessageId,
                timestamp = networkMessage.timestamp,
                message = null,
                spanStructureList = listOf(),
                actions = null,
                attachmentUrl = null,
                attachmentType = null,
                attachmentName = null,
                attachmentSize = null,
                operatorId = networkMessage.operatorId,
                operatorPreview = operatorPreview,
                operatorName = networkMessage.operatorName,
                height = null,
                width = null
            )
        }

    }

}