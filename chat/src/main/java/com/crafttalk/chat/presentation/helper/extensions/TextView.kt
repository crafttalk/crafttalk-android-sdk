package com.crafttalk.chat.presentation.helper.extensions

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.DisplayMetrics
import android.widget.TextView
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.message.MessageType
import com.crafttalk.chat.presentation.helper.ui.transformSizeDrawable
import com.crafttalk.chat.presentation.model.MessageModel
import com.crafttalk.chat.utils.ChatAttr
import java.text.SimpleDateFormat

fun TextView.setDrawableColor(color: Int) {
    compoundDrawables.filterNotNull().forEach {
        it.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}

@SuppressLint("SetTextI18n")
fun TextView.setTimeMessageWithCheck(message: MessageModel, scaleRatio: Float) {
    setTimeMessageDefault(message, scaleRatio)

    when (message.stateCheck) {
        MessageType.VISITOR_MESSAGE -> {}
        MessageType.RECEIVED_BY_MEDIATO -> {
            setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                transformSizeDrawable(
                    context,
                    R.drawable.ic_check,
                    (15 * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
                ),
                null
            )
        }
        MessageType.RECEIVED_BY_OPERATOR -> {
            setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                transformSizeDrawable(
                    context,
                    R.drawable.ic_db_check,
                    (15 * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
                ),
                null
            )
        }
    }

    setDrawableColor(ChatAttr.mapAttr["color_time_mark"] as Int)
}

@SuppressLint("SetTextI18n", "SimpleDateFormat")
fun TextView.setTimeMessageDefault(message: MessageModel, scaleRatio: Float) {
    val formatTime = SimpleDateFormat("dd.MM.yyyy HH:mm")
    // set content
    text = "${message.authorName} ${formatTime.format(message.timestamp)}"
    setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

    // set color
    setTextColor(ChatAttr.mapAttr["color_time_mark"] as Int)
    // set dimension
    textSize = (ChatAttr.mapAttr["size_time_mark"] as Float)/scaleRatio
}