package com.crafttalk.chat.presentation.helper.extensions

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
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

fun ImageView.settingMediaFile(isUnionMessageItem: Boolean = false) {
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
    warningContainer: ViewGroup? = null,
    isGif: Boolean = false
) {
    warningContainer?.visibility = View.GONE

    val (widthInPx, heightInPx) = getSizeScreenInPx(context)
    if (!mediaFile.failLoading) {
        layoutParams.width = if (mediaFile.height > mediaFile.width) (heightInPx * 0.4 * mediaFile.width / mediaFile.height).toInt() else (widthInPx * 0.7).toInt()
        layoutParams.height = if (mediaFile.height > mediaFile.width) (heightInPx * 0.4).toInt() else (widthInPx * 0.7 * mediaFile.height / mediaFile.width).toInt()
    } else {
        layoutParams.width = widthInPx / 2
        layoutParams.height = widthInPx / 2
    }

    Glide.with(context)
        .apply { if (isGif) asGif() }
        .load(mediaFile.url)
        .apply(RequestOptions().override(layoutParams.width, layoutParams.height))
        .placeholder(R.drawable.background_item_media_message_placeholder)
        .error(R.drawable.background_item_media_message_placeholder)
        .listener(
            object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    warningContainer?.apply {
                        layoutParams.width = widthInPx / 2 + ChatAttr.getInstance().marginStartMediaFile + ChatAttr.getInstance().marginEndMediaFile
                        layoutParams.height = widthInPx / 2 + ChatAttr.getInstance().marginTopMediaFile + ChatAttr.getInstance().marginBottomMediaFile
                        visibility = View.VISIBLE
                    }
                    return false
                }
                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    if (mediaFile.failLoading) {
                        resource ?: return false
                        updateData(idKey, resource.intrinsicHeight, resource.intrinsicWidth)
                    }
                    return false
                }
            }
        )
        .into(this)
}

fun ImageView.setFileIcon() {
    ChatAttr.getInstance().drawableFileIcon?.let {
        Glide.with(context)
            .load(it)
            .into(this)
    }
}