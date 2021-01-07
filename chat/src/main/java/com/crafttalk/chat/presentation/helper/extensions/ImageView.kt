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
import com.crafttalk.chat.presentation.model.FileModel

@SuppressLint("ResourceAsColor")
fun ImageView.loadImage(
    idKey: Long,
    imageFile: FileModel,
    updateData: (idKey: Long, height: Int, width: Int) -> Unit
) {
    val (widthInPx, heightInPx) = getSizeScreenInPx(context)
    if (imageFile.height == 0 && imageFile.width == 0) {
        Glide.with(context)
            .asBitmap()
            .load(imageFile.url)
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

                    updateData(idKey, resource.height, resource.width)

                }
            })
    } else {
        Glide.with(context)
            .load(imageFile.url)
            .apply(
                RequestOptions().override(
                if (imageFile.height > imageFile.width) (heightInPx * 0.4 * imageFile.width / imageFile.height).toInt() else (widthInPx * 0.7).toInt(),
                if (imageFile.height > imageFile.width) (heightInPx * 0.4).toInt() else (widthInPx * 0.7 * imageFile.height / imageFile.width).toInt()
            ))
            .error(R.color.default_color_company)
            .into(this)
    }
}

fun ImageView.loadGif(
    idKey: Long,
    gifFile: FileModel,
    updateData: (idKey: Long, height: Int, width: Int) -> Unit
) {
    val context = this.context
    val (widthInPx, heightInPx) = getSizeScreenInPx(context)
    if (gifFile.height == 0 && gifFile.width == 0) {
        Glide.with(context)
            .asBitmap()
            .load(gifFile.url)
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
                        .load(gifFile.url)
                        .apply(RequestOptions().override(newWidthInPx, newHeightInPx))
                        .error(R.color.default_color_company)
                        .into(this@loadGif)

                    updateData(idKey, resource.height, resource.width)

                }
            })
    } else {
        Glide.with(context)
            .asGif()
            .load(gifFile.url)
            .apply(
                RequestOptions().override(
                if (gifFile.height > gifFile.width) (heightInPx * 0.4 * gifFile.width / gifFile.height).toInt() else (widthInPx * 0.7).toInt(),
                if (gifFile.height > gifFile.width) (heightInPx * 0.4).toInt() else (widthInPx * 0.7 * gifFile.height / gifFile.width).toInt()
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