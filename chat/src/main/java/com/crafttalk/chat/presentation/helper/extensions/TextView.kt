package com.crafttalk.chat.presentation.helper.extensions

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.model.*
import com.crafttalk.chat.utils.ChatAttr
import com.crafttalk.chat.utils.ChatParams
import com.crafttalk.chat.utils.MediaFileDownloadMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat

fun TextView.setAuthor(message: MessageModel) {
    // set visibility / color / dimension
    if (message.role == Role.USER) {
        visibility = if (ChatAttr.getInstance().showUserMessageAuthor) {
            View.VISIBLE
        } else {
            View.GONE
        }
        setTextColor(ChatAttr.getInstance().colorTextUserMessageAuthor)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeUserMessageAuthor)
    } else {
        visibility = View.VISIBLE
        setTextColor(ChatAttr.getInstance().colorTextOperatorMessageAuthor)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeOperatorMessageAuthor)
    }
    // set font
    ChatAttr.getInstance().resFontFamilyMessageAuthor?.let {
        typeface = ResourcesCompat.getFont(context, it)
    }
    // set content
    text = message.authorName
}

@SuppressLint("SetTextI18n", "SimpleDateFormat")
fun TextView.setTime(message: MessageModel) {
    // set color / dimension
    if (message.role == Role.USER) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeUserMessageTime)
    } else {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeOperatorMessageTime)
    }
    when {
        message is TextMessageItem && message.role == Role.USER -> setTextColor(ChatAttr.getInstance().colorUserTextMessageTime)
        message is ImageMessageItem && message.role == Role.USER -> setTextColor(ChatAttr.getInstance().colorUserImageMessageTime)
        message is GifMessageItem && message.role == Role.USER -> setTextColor(ChatAttr.getInstance().colorUserGifMessageTime)
        message is FileMessageItem && message.role == Role.USER -> setTextColor(ChatAttr.getInstance().colorUserFileMessageTime)
        message is UnionMessageItem && message.role == Role.USER -> setTextColor(ChatAttr.getInstance().colorUserTextMessageTime)
        message is TextMessageItem && message.role == Role.OPERATOR -> setTextColor(ChatAttr.getInstance().colorOperatorTextMessageTime)
        message is ImageMessageItem && message.role == Role.OPERATOR -> setTextColor(ChatAttr.getInstance().colorOperatorImageMessageTime)
        message is GifMessageItem && message.role == Role.OPERATOR -> setTextColor(ChatAttr.getInstance().colorOperatorGifMessageTime)
        message is FileMessageItem && message.role == Role.OPERATOR -> setTextColor(ChatAttr.getInstance().colorOperatorFileMessageTime)
        message is UnionMessageItem && message.role == Role.OPERATOR -> setTextColor(ChatAttr.getInstance().colorOperatorTextMessageTime)
        message is TransferMessageItem -> setTextColor(ChatAttr.getInstance().colorOperatorTextMessageTime)
    }
    // set font
    ChatAttr.getInstance().resFontFamilyMessageTime?.let {
        typeface = ResourcesCompat.getFont(context, it)
    }
    // set content
    val formatTime = SimpleDateFormat("HH:mm")
    text = formatTime.format(message.timestamp)
}

@SuppressLint("SimpleDateFormat")
fun TextView.setDate(message: MessageModel) {
    if (message.isFirstMessageInDay) {
        // set content
        val formatYear = SimpleDateFormat("yyyy")
        val formatTime = SimpleDateFormat("dd MMMM", ChatParams.locale)

        val nowYear = formatYear.format(System.currentTimeMillis())
        val currentYear = formatYear.format(message.timestamp)
        val date = formatTime.format(message.timestamp)

        text = if (nowYear == currentYear) {
            date
        } else {
            "$date $currentYear"
        }
        // set color
        setTextColor(ChatAttr.getInstance().colorTextDateGrouping)
        // set dimension
        setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeTextDateGrouping)
        // set font
        ChatAttr.getInstance().resFontFamilyMessageDate?.let {
            typeface = ResourcesCompat.getFont(context, it)
        }
        visibility = View.VISIBLE
    } else {
        visibility = View.GONE
    }
}

fun TextView.settingDownloadBtn(isUserMessage: Boolean, failLoading: Boolean) {
    // set visible
    visibility = if (ChatAttr.getInstance().mediaFileDownloadMode in listOf(MediaFileDownloadMode.ONLY_IN_CHAT, MediaFileDownloadMode.All_PLACES) && !failLoading) {
        View.VISIBLE
    } else {
        View.GONE
    }
    // set color
    try {
        val xpp = resources.getXml(if (isUserMessage) ChatAttr.getInstance().colorUserFileMessageDownload else ChatAttr.getInstance().colorOperatorFileMessageDownload)
        val colorStateList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ColorStateList.createFromXml(resources, xpp, context.theme)
        } else {
            ColorStateList.createFromXml(resources, xpp)
        }
        setTextColor(colorStateList)
    } catch (e: Exception) {
        if (isUserMessage)
            setTextColor(ChatAttr.getInstance().colorUserFileMessageDownload)
        else
            setTextColor(ChatAttr.getInstance().colorOperatorFileMessageDownload)
    }
    // set dimension
    setTextSize(TypedValue.COMPLEX_UNIT_PX, if (isUserMessage) ChatAttr.getInstance().sizeUserFileMessageDownload else ChatAttr.getInstance().sizeOperatorFileMessageDownload)
    // set font
    (if (isUserMessage) ChatAttr.getInstance().resFontFamilyUserMessage else ChatAttr.getInstance().resFontFamilyOperatorMessage)?.let {
        typeface = ResourcesCompat.getFont(context, it)
    }
    // set bg
    setBackgroundResource(if (isUserMessage) ChatAttr.getInstance().backgroundUserFileMessageDownload else ChatAttr.getInstance().backgroundOperatorFileMessageDownload)
}

fun TextView.setFileName(file: FileModel, isUserMessage: Boolean) {
    text = file.name
    // set color and dimension
    if (isUserMessage) {
        setTextColor(ChatAttr.getInstance().colorUserFileName)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeUserFileName)
    } else {
        setTextColor(ChatAttr.getInstance().colorOperatorFileName)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeOperatorFileName)
    }
    // set font
    ChatAttr.getInstance().resFontFamilyFileInfo?.let {
        typeface = ResourcesCompat.getFont(context, it)
    }
}

fun TextView.setFileSize(file: FileModel, isUserMessage: Boolean) {
    if (file.size == 0L) return
    val df = DecimalFormat("#.##")
    val countByteInKByte = 1000L
    val countByteInMByte = 1000L * 1000L
    val countByteInGByte = 1000L * 1000L * 1000L
    text = when(file.size) {
        in 0L until countByteInKByte -> "${file.size} ${resources.getString(R.string.com_crafttalk_chat_file_size_byte)}"
        in countByteInKByte until countByteInMByte -> {
            val value = file.size.toDouble() / countByteInKByte
            "${(df.parse(df.format(value)).toDouble())} ${resources.getString(R.string.com_crafttalk_chat_file_size_Kb)}"
        }
        in countByteInMByte until countByteInGByte -> {
            val value = file.size.toDouble() / countByteInMByte
            "${(df.parse(df.format(value)).toDouble())} ${resources.getString(R.string.com_crafttalk_chat_file_size_Mb)}"
        }
        in countByteInGByte until countByteInGByte * 1000L -> {
            val value = file.size.toDouble() / countByteInGByte
            "${(df.parse(df.format(value)).toDouble())} ${resources.getString(R.string.com_crafttalk_chat_file_size_Gb)}"
        }
        else -> ""
    }
    // set color and dimension
    if (isUserMessage) {
        setTextColor(ChatAttr.getInstance().colorUserFileSize)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeUserFileSize)
    } else {
        setTextColor(ChatAttr.getInstance().colorOperatorFileSize)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeOperatorFileSize)
    }
    // set font
    ChatAttr.getInstance().resFontFamilyFileInfo?.let {
        typeface = ResourcesCompat.getFont(context, it)
    }
}