package com.crafttalk.chat.data.local.db.entity

enum class MessageViewType(val valueType: Int) {
    USER_TEXT_MESSAGE(1),
    USER_IMAGE_MESSAGE(2),
    USER_FILE_MESSAGE(3),
    SERVER_TEXT_MESSAGE(-1),
    SERVER_IMAGE_MESSAGE(-2),
    SERVER_FILE_MESSAGE(-3),
    DEFAULT_MESSAGE(0);


    companion object {
        fun getMessageViewType(messageObj: Message): MessageViewType {
            if (messageObj.isReply) {
                // serv
                if (messageObj.message == null || messageObj.message.isEmpty()) {
                    if (messageObj.attachmentType == null && messageObj.attachmentName == null && messageObj.attachmentUrl == null) {
                        return DEFAULT_MESSAGE
                    } else {
                        return when (messageObj.attachmentType) {
                            "IMAGE" -> SERVER_IMAGE_MESSAGE
                            "FILE" -> SERVER_FILE_MESSAGE
                            else -> DEFAULT_MESSAGE
                        }
                    }
                } else {
                    if (messageObj.attachmentType == null && messageObj.attachmentName == null && messageObj.attachmentUrl == null) {
                        return SERVER_TEXT_MESSAGE
                    } else {
                        // mix
                        return SERVER_TEXT_MESSAGE
                    }
                }
            } else {
                // user
                if (messageObj.message == null || messageObj.message.isEmpty()) {
                    if (messageObj.attachmentType == null && messageObj.attachmentName == null && messageObj.attachmentUrl == null) {
                        return DEFAULT_MESSAGE
                    } else {
                        return when (messageObj.attachmentType) {
                            "IMAGE" -> USER_IMAGE_MESSAGE
                            "FILE" -> USER_FILE_MESSAGE
                            else -> DEFAULT_MESSAGE
                        }
                    }
                } else {
                    if (messageObj.attachmentType == null && messageObj.attachmentName == null && messageObj.attachmentUrl == null) {
                        return USER_TEXT_MESSAGE
                    } else {
                        // mix
                        return USER_TEXT_MESSAGE
                    }
                }
            }
        }
    }
}
