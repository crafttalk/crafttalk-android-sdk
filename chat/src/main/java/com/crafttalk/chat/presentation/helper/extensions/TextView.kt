package com.crafttalk.chat.presentation.helper.extensions

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.helper.converters.convertDpToPx
import com.crafttalk.chat.presentation.model.FileModel
import com.crafttalk.chat.presentation.model.MessageModel
import com.crafttalk.chat.presentation.model.Role
import com.crafttalk.chat.utils.ChatAttr
import java.text.SimpleDateFormat

fun TextView.setAuthorIcon(message: MessageModel, hasAuthorIcon: Boolean = false) {
    when {
        hasAuthorIcon && message.authorPreview != null -> {
            Glide.with(context)
                .asDrawable()
                .load(message.authorPreview)
                .circleCrop()
                .apply(RequestOptions().override(convertDpToPx(24f, context).toInt(), convertDpToPx(24f, context).toInt()))
                .error(R.drawable.ic_operator)
                .into(object : CustomTarget<Drawable>() {
                    override fun onLoadCleared(placeholder: Drawable?) {}
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        setCompoundDrawablesWithIntrinsicBounds(resource, null, null, null)
                    }
                })
        }
        hasAuthorIcon && message.authorPreview == null -> {
            val authorIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_operator, null).apply {
                this?.setColorFilter(ChatAttr.getInstance().colorMain, PorterDuff.Mode.MULTIPLY)
            }
            setCompoundDrawablesWithIntrinsicBounds(authorIcon, null, null, null)
        }
        !hasAuthorIcon -> {
            setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }
}

fun TextView.setAuthor(message: MessageModel, hasAuthorIcon: Boolean = false) {
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
    setAuthorIcon(message, hasAuthorIcon)
    text = message.authorName
}

@SuppressLint("SetTextI18n", "SimpleDateFormat")
fun TextView.setTime(message: MessageModel) {
    // set color / dimension
    if (message.role == Role.USER) {
        setTextColor(ChatAttr.getInstance().colorTextUserMessageTime)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeUserMessageTime)
    } else {
        setTextColor(ChatAttr.getInstance().colorTextOperatorMessageTime)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeOperatorMessageTime)
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
        val formatTime = if (ChatAttr.getInstance().locale == null) {
            SimpleDateFormat("dd MMMM")
        } else {
            SimpleDateFormat("dd MMMM", ChatAttr.getInstance().locale)
        }

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

fun TextView.setFileName(file: FileModel) {
    text = file.name
    // set color
    setTextColor(ChatAttr.getInstance().colorTextFileName)
    // set dimension
    setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeTextFileName)
    // set font
    ChatAttr.getInstance().resFontFamilyFileInfo?.let {
        typeface = ResourcesCompat.getFont(context, it)
    }
}

fun TextView.setFileSize(file: FileModel) {
    if (file.size == 0L) return
    text = when(file.size) {
        in 0L until 1024L -> "${file.size} ${resources.getString(R.string.file_size_byte)}"
        in 1024L until 1024L * 1024L -> "${(file.size / 1024L).toInt()} ${resources.getString(R.string.file_size_Kb)}"
        in 1024L * 1024L until 1024L * 1024L * 1024L -> "${(file.size / (1024L * 1024L)).toInt()} ${resources.getString(R.string.file_size_Mb)}"
        in 1024L * 1024L * 1024L until 1024L * 1024L * 1024L * 1024L -> "${(file.size / (1024L * 1024L * 1024L)).toInt()} ${resources.getString(R.string.file_size_Gb)}"
        else -> ""
    }
    // set color
    setTextColor(ChatAttr.getInstance().colorTextFileSize)
    // set dimension
    setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeTextFileSize)
    // set font
    ChatAttr.getInstance().resFontFamilyFileInfo?.let {
        typeface = ResourcesCompat.getFont(context, it)
    }
}