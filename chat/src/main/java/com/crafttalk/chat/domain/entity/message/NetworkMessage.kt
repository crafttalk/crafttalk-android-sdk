package com.crafttalk.chat.domain.entity.message

import com.crafttalk.chat.data.local.db.entity.MessageEntity
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.utils.ChatParams
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NetworkMessage (

    @SerializedName (value = "id")
    var id: String?,

    @SerializedName (value = "id_from_channel")
    val idFromChannel: String? = null,

    @SerializedName (value = "message_type")
    var messageType: Int,

    @SerializedName (value = "isReply")
    val isReply : Boolean,

    @SerializedName (value = "parent_message_id", alternate = ["parent_msg_id"])
    val parentMessageId: String? = null,

    @SerializedName (value = "timestamp")
    val timestamp: Long,

    @SerializedName (value = "message")
    var message: String? = null,

    @SerializedName (value = "widget")
    val widget: NetworkWidget? = null,

    @SerializedName (value = "action")
    val selectedAction: String? = null,

    @SerializedName (value = "actions")
    val actions: List<NetworkAction>? = null,

    @SerializedName (value = "keyboard")
    val keyboard: NetworkKeyboard? = null,

    @SerializedName (value = "attachment_url")
    var attachmentUrl: String? = null,

    @SerializedName (value = "attachment_type")
    var attachmentType: String? = null,

    @SerializedName (value = "attachment_name")
    var attachmentName: String? = null,

    @SerializedName (value = "operator_id")
    val operatorId: String? = null,

    @SerializedName (value = "operator_name")
    val operatorName: String? = null,

    @SerializedName (value = "reply_to_message")
    val replyToMessage: NetworkMessage? = null,

    @SerializedName (value = "dialog_id")
    val dialogId: String? = null

) : Serializable {
    val isText: Boolean
    get() = !message.isNullOrBlank()

    val isImage: Boolean
    get() = !attachmentUrl.isNullOrEmpty() &&
            !attachmentName.isNullOrEmpty() &&
            !attachmentType.isNullOrEmpty() &&
            (attachmentType == "IMAGE" || attachmentType!!.toLowerCase(
                ChatParams.locale!!).startsWith("image"))
    val isGif: Boolean
    get() = !attachmentUrl.isNullOrEmpty() &&
            !attachmentName.isNullOrEmpty() &&
            !attachmentType.isNullOrEmpty() &&
            (attachmentType == "IMAGE" || attachmentType!!.toLowerCase(
                ChatParams.locale!!).startsWith("image")) &&
            attachmentName!!.contains(".GIF", true)

    val isFile: Boolean
    get() = !attachmentUrl.isNullOrEmpty() &&
            !attachmentName.isNullOrEmpty() &&
            !attachmentType.isNullOrEmpty() &&
            attachmentType == "FILE"

    val isMarkdownFile: Boolean
        get() = message!!.contains("ct-markdown__file")

    /**Отличие от обычного IsImage -- тут нет проверки attachmentName
     * **/
    val isMarkdownImage: Boolean
        get() = !attachmentUrl.isNullOrEmpty() &&
                attachmentTypeFile != TypeFile.STICKER &&
                !attachmentType.isNullOrEmpty() &&
                (attachmentType == "IMAGE" || attachmentType!!.toLowerCase(
                    ChatParams.locale!!).startsWith("image"))
    val isMarkdownGif: Boolean
        get() = !attachmentUrl.isNullOrEmpty() &&
                !attachmentType.isNullOrEmpty() &&
                (attachmentType == "IMAGE" || attachmentType!!.toLowerCase(
                    ChatParams.locale!!).startsWith("image")) &&
                attachmentName!!.contains(".GIF", true)

    val isUnknownType: Boolean
        get() = !attachmentUrl.isNullOrEmpty() &&
                !attachmentName.isNullOrEmpty() &&
                !attachmentType.isNullOrEmpty()

    val isSticker: Boolean
        get() = !attachmentUrl.isNullOrEmpty() &&
                !attachmentType.isNullOrEmpty() &&
                attachmentType!!.contains("IMAGE/STICKER", ignoreCase = true)

    val isContainsContent: Boolean
    get() = isText || isImage || isGif || isFile || isSticker

    val attachmentTypeFile: TypeFile?
    get() = when {
        isFile -> TypeFile.FILE
        isImage -> TypeFile.IMAGE
        isGif -> TypeFile.GIF
        isSticker -> TypeFile.STICKER
        isUnknownType -> TypeFile.FILE
        else -> null
    }

    var correctAttachmentUrl: String? = ""
    get() = if (attachmentUrl?.startsWith("/webchat/file/") == true) {
        "${ChatParams.urlChatScheme}://${ChatParams.urlChatHost}${attachmentUrl}"
    } else {
        attachmentUrl
    }

    companion object {

        fun map(messageEntity: MessageEntity) = NetworkMessage(
            id = messageEntity.id,
            messageType = messageEntity.messageType,
            isReply = messageEntity.isReply,
            parentMessageId = messageEntity.parentMsgId,
            timestamp = messageEntity.timestamp,
            message = messageEntity.message,
            widget = messageEntity.widget?.let { NetworkWidget.map(it) },
            actions = messageEntity.actions?.map { NetworkAction.map(it) },
            keyboard = messageEntity.keyboard?.let { NetworkKeyboard.map(it) },
            attachmentUrl = messageEntity.attachmentUrl,
            attachmentType = messageEntity.attachmentType?.name,
            attachmentName = messageEntity.attachmentName,
            operatorId = messageEntity.operatorId,
            operatorName = messageEntity.operatorName,
            replyToMessage = null,
            dialogId = messageEntity.dialogId
        )
    }
}