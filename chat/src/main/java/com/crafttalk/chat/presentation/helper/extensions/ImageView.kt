package com.crafttalk.chat.presentation.helper.extensions

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.message.MessageType
import com.crafttalk.chat.presentation.helper.ui.getSizeScreenInPx
import com.crafttalk.chat.presentation.model.FileModel
import com.crafttalk.chat.presentation.model.MessageModel
import com.crafttalk.chat.presentation.model.Role
import com.crafttalk.chat.utils.ChatAttr

fun ImageView.setStatusMessage(message: MessageModel) {
    if (message.role == Role.USER && ChatAttr.getInstance().showUserMessageStatus) {
        visibility = when (message.stateCheck) {
            MessageType.RECEIVED_BY_MEDIATO -> {
                Glide.with(context)
                    .load(R.drawable.ic_check)
                    .into(this)
                View.VISIBLE
            }
            MessageType.RECEIVED_BY_OPERATOR -> {
                Glide.with(context)
                    .load(R.drawable.ic_db_check)
                    .into(this)
                View.VISIBLE
            }
            else -> View.GONE
        }
        setColorFilter(ChatAttr.getInstance().colorUserMessageStatus)
    } else {
        visibility = View.GONE
    }
}

fun ImageView.settingMediaFile(
    mediaFile: FileModel,
    urlFromHolder: String?,
    container: ViewGroup? = null,
    isUnionMessageItem: Boolean = false
) {
    if (mediaFile.height == 0 && mediaFile.width == 0) {
        visibility = View.GONE
        container?.visibility = View.GONE
    } else {
        if (urlFromHolder != mediaFile.url) {
            setImageResource(R.drawable.background_item_media_message_placeholder)
        }
        visibility = View.VISIBLE
        container?.visibility = View.VISIBLE
    }
    if (!isUnionMessageItem) {
        (layoutParams as ViewGroup.MarginLayoutParams).setMargins(
            ChatAttr.getInstance().marginStartMediaFile,
            ChatAttr.getInstance().marginTopMediaFile,
            ChatAttr.getInstance().marginEndMediaFile,
            ChatAttr.getInstance().marginBottomMediaFile
        )
    }
}

@SuppressLint("ResourceAsColor")
fun ImageView.loadMediaFile(
    idKey: Long,
    mediaFile: FileModel,
    updateData: (idKey: Long, height: Int, width: Int) -> Unit,
    isGif: Boolean = false
) {
    val (widthInPx, heightInPx) = getSizeScreenInPx(context)
    if (mediaFile.height == 0 && mediaFile.width == 0) {
        Glide.with(context)
            .asBitmap()
            .load(mediaFile.url)
            .error(R.drawable.background_item_media_message_placeholder)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    setImageDrawable(placeholder)
                }
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    updateData(idKey, resource.height, resource.width)
                }
            })
    } else {
        layoutParams.width = if (mediaFile.height > mediaFile.width) (heightInPx * 0.4 * mediaFile.width / mediaFile.height).toInt() else (widthInPx * 0.7).toInt()
        layoutParams.height = if (mediaFile.height > mediaFile.width) (heightInPx * 0.4).toInt() else (widthInPx * 0.7 * mediaFile.height / mediaFile.width).toInt()

        Glide.with(context)
            .apply { if (isGif) asGif() }
            .load(mediaFile.url)
            .apply(RequestOptions().override(layoutParams.width, layoutParams.height))
            .placeholder(R.drawable.background_item_media_message_placeholder)
            .error(R.drawable.background_item_media_message_placeholder)
            .into(this)
    }
}

fun ImageView.setFileIcon() {
    ChatAttr.getInstance().drawableFileIcon?.let {
        Glide.with(context)
            .load(it)
            .into(this)
    }
}