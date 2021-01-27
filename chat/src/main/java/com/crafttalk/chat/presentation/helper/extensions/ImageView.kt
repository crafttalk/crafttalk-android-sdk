package com.crafttalk.chat.presentation.helper.extensions

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.helper.ui.getSizeScreenInPx
import com.crafttalk.chat.presentation.model.FileModel

fun ImageView.settingMediaFile(
    mediaFile: FileModel,
    urlFromHolder: String?,
    timeView: View? = null
) {
    if (mediaFile.height == 0 && mediaFile.width == 0) {
        visibility = View.GONE
        timeView?.visibility = View.GONE
    } else {
        if (urlFromHolder != mediaFile.url) {
            setImageResource(R.drawable.background_item_media_message_placeholder)
        }
        visibility = View.VISIBLE
        timeView?.visibility = View.VISIBLE
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
    val (widthInPx, heightInPx) = getSizeScreenInPx(context)

    layoutParams.let {
        it.height = (widthInPx * 0.1).toInt()
        it.width = (widthInPx * 0.1).toInt()
    }
}