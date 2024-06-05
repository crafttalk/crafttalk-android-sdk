package com.crafttalk.chat.data.local.db.entity

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.crafttalk.chat.data.helper.converters.text.convertTextToNormalString
import com.crafttalk.chat.data.local.db.entity.MessageEntity.Companion.TABLE_NAME
import com.crafttalk.chat.domain.entity.file.TypeDownloadProgress
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.message.MessageType
import com.crafttalk.chat.domain.entity.message.NetworkButtonOperation
import com.crafttalk.chat.domain.entity.tags.Tag
import kotlin.math.abs
import com.crafttalk.chat.domain.entity.message.NetworkMessage
import com.crafttalk.chat.utils.ConstantsUtils.TAG_MESSAGE_ENTITY_DEBUG
import com.google.gson.annotations.SerializedName
import java.util.*

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
    val parentMsgId: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "arrival_time")
    val arrivalTime: Long? = null,

    @ColumnInfo(name = "message")
    val message: String? = null,

    @ColumnInfo(name = "span_structure_list")
    val spanStructureList: List<Tag> = listOf(),

    @ColumnInfo(name = "widget")
    val widget: WidgetEntity? = null,

    @ColumnInfo(name = "actions")
    val actions: List<ActionEntity>? = null,

    @SerializedName(value = "keyboard")
    val keyboard: KeyboardEntity? = null,

    @ColumnInfo(name = "attachment_url")
    val attachmentUrl: String? = null,

    @ColumnInfo(name = "attachment_type")
    val attachmentType: TypeFile? = null,

    @ColumnInfo(name = "attachment_download_progress_type")
    val attachmentDownloadProgressType: TypeDownloadProgress? = null,

    @ColumnInfo(name = "attachment_name")
    val attachmentName: String? = null,

    @ColumnInfo(name = "attachment_size")
    val attachmentSize: Long? = null,

    @ColumnInfo(name = "operator_id")
    val operatorId: String? = null,

    @ColumnInfo(name = "operator_preview")
    val operatorPreview: String? = null,

    @ColumnInfo(name = "operator_name")
    val operatorName: String? = null,

    @ColumnInfo(name = "height")
    val height: Int? = null,

    @ColumnInfo(name = "width")
    val width: Int? = null,

    @ColumnInfo(name = "replied_message_id")
    val repliedMessageId: String? = null,

    @ColumnInfo(name = "replied_message_text")
    val repliedMessageText: String? = null,

    @ColumnInfo(name = "replied_message_span_structure_list")
    val repliedTextSpanStructureList: List<Tag> = listOf(),

    @ColumnInfo(name = "replied_message_attachment_url")
    val repliedMessageAttachmentUrl: String? = null,

    @ColumnInfo(name = "replied_message_attachment_type")
    val repliedMessageAttachmentType: TypeFile? = null,

    @ColumnInfo(name = "replied_message_attachment_name")
    val repliedMessageAttachmentName: String? = null,

    @ColumnInfo(name = "replied_message_attachment_size")
    val repliedMessageAttachmentSize: Long? = null,

    @ColumnInfo(name = "replied_message_attachment_download_progress_type")
    val repliedMessageAttachmentDownloadProgressType: TypeDownloadProgress? = null,

    @ColumnInfo(name = "replied_message_attachment_height")
    val repliedMessageAttachmentHeight: Int? = null,

    @ColumnInfo(name = "replied_message_attachment_width")
    val repliedMessageAttachmentWidth: Int? = null,

    @ColumnInfo(name = "dialog_id")
    val dialogId: String? = null
) {

    fun hasSelectedAction(): Boolean {
        return if (actions == null) {
            false
        } else {
            actions.find { it.isSelected } != null
        }
    }

    fun hasSelectedButton(): Boolean {
        var hasActionBtn = false
        var hasSelectedActionBtn = false
        keyboard?.buttons?.forEach { horizontalList ->
            horizontalList.forEach { button ->
                if (button.typeOperation == NetworkButtonOperation.ACTION) {
                    hasActionBtn = true
                    if (button.selected) {
                        hasSelectedActionBtn = true
                    }
                }
            }
        }
        return if (keyboard?.buttons.isNullOrEmpty() || !hasActionBtn) {
            false
        } else {
            hasSelectedActionBtn
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
            arrivalTime: Long,
            operatorPreview: String?,
            fileSize: Long? = null,
            mediaFileHeight: Int? = null,
            mediaFileWidth: Int? = null,
            repliedMessageFileSize: Long? = null,
            repliedMessageMediaFileHeight: Int? = null,
            repliedMessageMediaFileWidth: Int? = null,
        ): MessageEntity {
            val list = arrayListOf<Tag>()
            Log.d(TAG_MESSAGE_ENTITY_DEBUG,"Got common message")
            val message = networkMessage.message?.convertTextToNormalString(list)
            val repliedList = arrayListOf<Tag>()
            val repliedMessage = networkMessage.replyToMessage?.message?.convertTextToNormalString(repliedList)

            return MessageEntity(
                uuid = uuid,
                id = networkMessage.id!!,
                messageType = networkMessage.messageType,
                isReply = networkMessage.isReply,
                parentMsgId = networkMessage.parentMessageId,
                timestamp = networkMessage.timestamp,
                arrivalTime = arrivalTime,
                message = message,
                spanStructureList = list,
                widget = if (networkMessage.isReply) networkMessage.widget?.let { WidgetEntity.map(it) } else null,
                actions = if (networkMessage.isReply) networkMessage.actions?.let { ActionEntity.map(it) } else null,
                keyboard = if (networkMessage.isReply) networkMessage.keyboard?.let { KeyboardEntity.map(it, listOf()) } else null,
                attachmentUrl = networkMessage.correctAttachmentUrl,
                attachmentType = networkMessage.attachmentTypeFile,
                attachmentDownloadProgressType = TypeDownloadProgress.NOT_DOWNLOADED,
                attachmentName = networkMessage.attachmentName,
                attachmentSize = fileSize,
                operatorId = networkMessage.operatorId,
                operatorPreview = operatorPreview,
                operatorName = if (networkMessage.isReply) networkMessage.operatorName else "Вы",
                height = mediaFileHeight,
                width = mediaFileWidth,
                repliedMessageId = networkMessage.replyToMessage?.id,
                repliedMessageText = repliedMessage,
                repliedTextSpanStructureList = repliedList,
                repliedMessageAttachmentUrl = networkMessage.replyToMessage?.correctAttachmentUrl,
                repliedMessageAttachmentType = networkMessage.replyToMessage?.attachmentTypeFile,
                repliedMessageAttachmentName = networkMessage.replyToMessage?.attachmentName,
                repliedMessageAttachmentSize = repliedMessageFileSize,
                repliedMessageAttachmentDownloadProgressType = TypeDownloadProgress.NOT_DOWNLOADED,
                repliedMessageAttachmentHeight = repliedMessageMediaFileHeight,
                repliedMessageAttachmentWidth = repliedMessageMediaFileWidth,
                dialogId = networkMessage.dialogId
            )
        }

        fun mapOperatorMessage(
            uuid: String,
            networkMessage: NetworkMessage,
            arrivalTime: Long,
            actionsSelected: List<String>,
            buttonsSelected: List<String>,
            operatorPreview: String?,
            fileSize: Long? = null,
            mediaFileHeight: Int? = null,
            mediaFileWidth: Int? = null
        ): MessageEntity {
            val list = arrayListOf<Tag>()
            Log.d(TAG_MESSAGE_ENTITY_DEBUG,"Got operator message")
            val message = networkMessage.message?.convertTextToNormalString(list)

            return MessageEntity(
                uuid = uuid,
                id = networkMessage.id!!,
                messageType = networkMessage.messageType,
                isReply = true,
                parentMsgId = networkMessage.parentMessageId,
                timestamp = networkMessage.timestamp,
                arrivalTime = arrivalTime,
                message = message,
                spanStructureList = list,
                widget = networkMessage.widget?.let { WidgetEntity.map(it) },
                actions = networkMessage.actions?.let { ActionEntity.map(it, actionsSelected) },
                keyboard = networkMessage.keyboard?.let { KeyboardEntity.map(it, buttonsSelected) },
                attachmentUrl = networkMessage.correctAttachmentUrl,
                attachmentType = networkMessage.attachmentTypeFile,
                attachmentDownloadProgressType = TypeDownloadProgress.NOT_DOWNLOADED,
                attachmentName = networkMessage.attachmentName,
                attachmentSize = fileSize,
                operatorId = networkMessage.operatorId,
                operatorPreview = operatorPreview,
                operatorName = networkMessage.operatorName,
                height = mediaFileHeight,
                width = mediaFileWidth,
                dialogId = networkMessage.dialogId
            )
        }

        fun mapUserMessage(
            uuid: String,
            networkMessage: NetworkMessage,
            arrivalTime: Long,
            status: Int,
            operatorPreview: String?,
            fileSize: Long? = null,
            mediaFileHeight: Int? = null,
            mediaFileWidth: Int? = null,
            repliedMessageFileSize: Long? = null,
            repliedMessageMediaFileHeight: Int? = null,
            repliedMessageMediaFileWidth: Int? = null,
        ): MessageEntity {
            val list = arrayListOf<Tag>()
            Log.d(TAG_MESSAGE_ENTITY_DEBUG,"Got user message")
            val message = networkMessage.message?.convertTextToNormalString(list)
            val repliedList = arrayListOf<Tag>()
            val repliedMessage = networkMessage.replyToMessage?.message?.convertTextToNormalString(repliedList)

            return MessageEntity(
                uuid = uuid,
                id = networkMessage.idFromChannel ?: networkMessage.id!!,
                messageType = status,
                isReply = false,
                parentMsgId = networkMessage.parentMessageId,
                timestamp = networkMessage.timestamp,
                arrivalTime = arrivalTime,
                message = message,
                spanStructureList = list,
                attachmentUrl = networkMessage.correctAttachmentUrl,
                attachmentType = networkMessage.attachmentTypeFile,
                attachmentDownloadProgressType = TypeDownloadProgress.NOT_DOWNLOADED,
                attachmentName = networkMessage.attachmentName,
                attachmentSize = fileSize,
                operatorId = networkMessage.operatorId,
                operatorPreview = operatorPreview,
                operatorName = "Вы",
                height = mediaFileHeight,
                width = mediaFileWidth,
                repliedMessageId = networkMessage.replyToMessage?.id,
                repliedMessageText = repliedMessage,
                repliedTextSpanStructureList = repliedList,
                repliedMessageAttachmentUrl = networkMessage.replyToMessage?.correctAttachmentUrl,
                repliedMessageAttachmentType = networkMessage.replyToMessage?.attachmentTypeFile,
                repliedMessageAttachmentName = networkMessage.replyToMessage?.attachmentName,
                repliedMessageAttachmentSize = repliedMessageFileSize,
                repliedMessageAttachmentDownloadProgressType = TypeDownloadProgress.NOT_DOWNLOADED,
                repliedMessageAttachmentHeight = repliedMessageMediaFileHeight,
                repliedMessageAttachmentWidth = repliedMessageMediaFileWidth,
                dialogId = networkMessage.dialogId
            )
        }

        fun mapOperatorJoinMessage(
            uuid: String,
            networkMessage: NetworkMessage,
            arrivalTime: Long,
            operatorPreview: String?
        ): MessageEntity {
            return MessageEntity(
                uuid = uuid,
                id = networkMessage.id!!,
                messageType = networkMessage.messageType,
                isReply = true,
                parentMsgId = networkMessage.parentMessageId,
                timestamp = networkMessage.timestamp,
                arrivalTime = arrivalTime,
                operatorId = networkMessage.operatorId,
                operatorPreview = operatorPreview,
                operatorName = networkMessage.operatorName,
                dialogId = networkMessage.dialogId
            )
        }

        fun mapInfoMessage(
            uuid: String,
            infoMessage: String,
            timestamp: Long,
            arrivalTime: Long
        ): MessageEntity {
            val list = arrayListOf<Tag>()
            Log.d(TAG_MESSAGE_ENTITY_DEBUG,"Got info message")
            val message = infoMessage.convertTextToNormalString(list)

            return MessageEntity(
                uuid = uuid,
                id = UUID.randomUUID().toString(),
                messageType = MessageType.INFO_MESSAGE.valueType,
                isReply = true,
                timestamp = timestamp,
                arrivalTime = arrivalTime,
                message = message,
                spanStructureList = list
            )
        }
    }
}