package com.crafttalk.chat.presentation.helper.extensions

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.message.MessageType
import com.crafttalk.chat.presentation.helper.converters.convertDpToPx
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
fun TextView.setTimeMessageWithCheck(message: MessageModel) {
    setTimeMessageDefault(message)

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

    setDrawableColor(ChatAttr.getInstance().colorTextTimeMark)
}

@SuppressLint("SetTextI18n", "SimpleDateFormat")
fun TextView.setTimeMessageDefault(message: MessageModel, hasAuthorIcon: Boolean = false) {
    val formatTime = SimpleDateFormat("HH:mm")
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
    // set content
    text = "${message.authorName} ${formatTime.format(message.timestamp)}"
    // set color
    setTextColor(ChatAttr.getInstance().colorTextTimeMark)
    // set dimension
    setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeTextTimeMark)
}

@SuppressLint("SimpleDateFormat")
fun TextView.setDate(message: MessageModel) {
    if (message.isFirstMessageInDay) {
        // set content
        val formatTime = SimpleDateFormat("dd.MM.yyyy")
        text = formatTime.format(message.timestamp)
        // set color
        setTextColor(ChatAttr.getInstance().colorTextDateGrouping)
        // set dimension
        setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeTextDateGrouping)
        visibility = View.VISIBLE
    } else {
        visibility = View.GONE
    }
}
