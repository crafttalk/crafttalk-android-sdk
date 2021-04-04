package com.crafttalk.chat.presentation.helper.extensions

import android.content.res.TypedArray
import androidx.annotation.AnyRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes

@AnyRes
fun TypedArray.getResourceIdOrNull(@StyleableRes index: Int): Int? {
    val result = getResourceId(index, -1)
    return if (result == -1) null else result
}

fun TypedArray.getDimensionOrNull(@StyleableRes index: Int): Float? {
    val result = getDimension(index, -1f)
    return if (result == -1f) null else result
}

@ColorInt
fun TypedArray.getColorOrNull(@StyleableRes index: Int): Int? {
    val result = getColor(index, -1)
    return if (result == -1) null else result
}