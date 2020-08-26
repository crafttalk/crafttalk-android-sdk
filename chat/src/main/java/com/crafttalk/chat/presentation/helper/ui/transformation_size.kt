package com.crafttalk.chat.presentation.helper.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes

fun transformSizeDrawable(context: Context, @DrawableRes idIcon: Int, newSize: Int): Drawable {
    // dont work in Xiaomi
//        val dr = ResourcesCompat.getDrawable(inflater.context.resources, idIcon, null)
//        val bitmap = (dr as BitmapDrawable).bitmap

    val bitmap = BitmapFactory.decodeResource(context.resources, idIcon)
    return BitmapDrawable(context.resources, Bitmap.createScaledBitmap(bitmap, newSize, newSize, true))
}