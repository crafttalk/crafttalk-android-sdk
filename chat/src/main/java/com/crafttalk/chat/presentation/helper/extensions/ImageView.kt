package com.crafttalk.chat.presentation.helper.extensions

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.helper.ui.getSizeScreenInPx
import com.crafttalk.chat.presentation.model.GifMessageItem
import com.crafttalk.chat.presentation.model.ImageMessageItem

@SuppressLint("ResourceAsColor")
fun ImageView.loadImage(
    imageMessage: ImageMessageItem,
    updateData: (idKey: Long, height: Int, width: Int) -> Unit
) {
    val (widthInPx, heightInPx) = getSizeScreenInPx(context)
    if (imageMessage.height == 0 && imageMessage.width == 0) {
        Glide.with(context)
            .asBitmap()
            .load(imageMessage.imageUrl)
            .error(R.color.default_color_company)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    setImageDrawable(placeholder)
                }
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val (newWidthInPx, newHeightInPx) =
                        if (resource.height > resource.width) {
                            Pair((heightInPx * 0.4 * resource.width / resource.height).toInt(), (heightInPx * 0.4).toInt())
                        } else {
                            Pair((widthInPx * 0.7).toInt(), (widthInPx * 0.7 * resource.height / resource.width).toInt())
                        }

                    setImageBitmap(
                        Bitmap.createScaledBitmap(
                            resource,
                            newWidthInPx,
                            newHeightInPx,
                            false
                        )
                    )

                    updateData(imageMessage.idKey, resource.height, resource.width)

                }
            })
    } else {
        Glide.with(context)
            .load(imageMessage.imageUrl)
            .apply(
                RequestOptions().override(
                if (imageMessage.height > imageMessage.width) (heightInPx * 0.4 * imageMessage.width / imageMessage.height).toInt() else (widthInPx * 0.7).toInt(),
                if (imageMessage.height > imageMessage.width) (heightInPx * 0.4).toInt() else (widthInPx * 0.7 * imageMessage.height / imageMessage.width).toInt()
            ))
            .error(R.color.default_color_company)
            .into(this)
    }
}

fun ImageView.loadGif(
    gifMessage: GifMessageItem,
    updateData: (idKey: Long, height: Int, width: Int) -> Unit
) {
    val context = this.context
    val (widthInPx, heightInPx) = getSizeScreenInPx(context)
    if (gifMessage.height == 0 && gifMessage.width == 0) {
        Glide.with(context)
            .asBitmap()
            .load(gifMessage.gifUrl)
            .error(R.color.default_color_company)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    setImageDrawable(placeholder)
                }
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val (newWidthInPx, newHeightInPx) =
                        if (resource.height > resource.width) {
                            Pair((heightInPx * 0.4 * resource.width / resource.height).toInt(), (heightInPx * 0.4).toInt())
                        } else {
                            Pair((widthInPx * 0.7).toInt(), (widthInPx * 0.7 * resource.height / resource.width).toInt())
                        }

                    Glide.with(context)
                        .asGif()
                        .load(gifMessage.gifUrl)
                        .apply(RequestOptions().override(newWidthInPx, newHeightInPx))
                        .error(R.color.default_color_company)
                        .into(this@loadGif)

                    updateData(gifMessage.idKey, resource.height, resource.width)

                }
            })
    } else {
        Glide.with(context)
            .asGif()
            .load(gifMessage.gifUrl)
            .apply(
                RequestOptions().override(
                if (gifMessage.height > gifMessage.width) (heightInPx * 0.4 * gifMessage.width / gifMessage.height).toInt() else (widthInPx * 0.7).toInt(),
                if (gifMessage.height > gifMessage.width) (heightInPx * 0.4).toInt() else (widthInPx * 0.7 * gifMessage.height / gifMessage.width).toInt()
            ))
            .error(R.color.default_color_company)
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