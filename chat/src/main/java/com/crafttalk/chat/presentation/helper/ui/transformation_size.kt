package com.crafttalk.chat.presentation.helper.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes

fun transformSizeDrawable(context: Context, @DrawableRes idIcon: Int, newSize: Int): Drawable? {
    val bitmap: Bitmap? = BitmapFactory.decodeResource(context.resources, idIcon)
    return bitmap?.let { BitmapDrawable(context.resources, Bitmap.createScaledBitmap(it, newSize, newSize, false)) }
}