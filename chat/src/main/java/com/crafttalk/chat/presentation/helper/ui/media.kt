package com.crafttalk.chat.presentation.helper.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

fun getSizeMediaFile(context: Context, url: String, resultSize: (height: Int?, width: Int?) -> Unit) {
    Glide.with(context)
        .asBitmap()
        .load(url)
        .into(object : CustomTarget<Bitmap>() {
            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                resultSize(null, null)
            }
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                resultSize(resource.height, resource.width)
            }
            override fun onLoadCleared(placeholder: Drawable?) {}
        })
}