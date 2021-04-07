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
import com.crafttalk.chat.presentation.model.*
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
        when (message) {
            is TextMessageItem -> setColorFilter(ChatAttr.getInstance().colorUserTextMessageStatus)
            is ImageMessageItem -> setColorFilter(ChatAttr.getInstance().colorUserImageMessageStatus)
            is GifMessageItem -> setColorFilter(ChatAttr.getInstance().colorUserGifMessageStatus)
            is FileMessageItem -> setColorFilter(ChatAttr.getInstance().colorUserFileMessageStatus)
            is UnionMessageItem -> setColorFilter(ChatAttr.getInstance().colorUserTextMessageStatus)
        }
    } else {
        visibility = View.GONE
    }
}

fun ImageView.setAuthorIcon(authorPreview: String? = null, showAuthorIcon: Boolean = true) {
    if (showAuthorIcon) {
        Glide.with(context)
            .load(authorPreview ?: R.drawable.ic_operator)
            .circleCrop()
            .apply(
                RequestOptions().override(
                    ChatAttr.getInstance().sizeOperatorMessageAuthorPreview,
                    ChatAttr.getInstance().sizeOperatorMessageAuthorPreview
                )
            )
            .error(R.drawable.ic_operator)
            .listener(
                object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        setColorFilter(ChatAttr.getInstance().colorMain)
                        return false
                    }
                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        if (authorPreview == null) {
                            setColorFilter(ChatAttr.getInstance().colorMain)
                        } else {
                            colorFilter = null
                        }
                        return false
                    }
                }
            )
            .into(this)
        visibility = View.VISIBLE
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
    isUserMessage: Boolean,
    warningContainer: ViewGroup? = null,
    isGif: Boolean = false
) {
    warningContainer?.visibility = View.GONE

    when {
        !mediaFile.failLoading && mediaFile.height > mediaFile.width && isUserMessage -> {
            layoutParams.width = ChatAttr.getInstance().heightElongatedItemUserFilePreviewMessage * mediaFile.width / mediaFile.height
            layoutParams.height = ChatAttr.getInstance().heightElongatedItemUserFilePreviewMessage
        }
        !mediaFile.failLoading && mediaFile.height <= mediaFile.width && isUserMessage -> {
            layoutParams.width = ChatAttr.getInstance().widthElongatedItemUserFilePreviewMessage
            layoutParams.height = ChatAttr.getInstance().widthElongatedItemUserFilePreviewMessage * mediaFile.height / mediaFile.width
        }
        !mediaFile.failLoading && mediaFile.height > mediaFile.width && !isUserMessage -> {
            layoutParams.width = ChatAttr.getInstance().heightElongatedItemOperatorFilePreviewMessage * mediaFile.width / mediaFile.height
            layoutParams.height = ChatAttr.getInstance().heightElongatedItemOperatorFilePreviewMessage
        }
        !mediaFile.failLoading && mediaFile.height <= mediaFile.width && !isUserMessage -> {
            layoutParams.width = ChatAttr.getInstance().widthElongatedItemOperatorFilePreviewMessage
            layoutParams.height = ChatAttr.getInstance().widthElongatedItemOperatorFilePreviewMessage * mediaFile.height / mediaFile.width
        }
        mediaFile.failLoading && isUserMessage -> {
            layoutParams.width = ChatAttr.getInstance().widthItemUserFilePreviewWarningMessage
            layoutParams.height = ChatAttr.getInstance().widthItemUserFilePreviewWarningMessage
        }
        mediaFile.failLoading && !isUserMessage -> {
            layoutParams.width = ChatAttr.getInstance().widthItemOperatorFilePreviewWarningMessage
            layoutParams.height = ChatAttr.getInstance().widthItemOperatorFilePreviewWarningMessage
        }
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
                        layoutParams.width = this@loadMediaFile.layoutParams.width + ChatAttr.getInstance().marginStartMediaFile + ChatAttr.getInstance().marginEndMediaFile
                        layoutParams.height = this@loadMediaFile.layoutParams.height + ChatAttr.getInstance().marginTopMediaFile + ChatAttr.getInstance().marginBottomMediaFile
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