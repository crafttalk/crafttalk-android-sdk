package com.crafttalk.chat.presentation.helper.extensions

import android.content.res.TypedArray
import androidx.annotation.AnyRes
import androidx.annotation.StyleableRes

@AnyRes
fun TypedArray.getResourceIdOrNull(@StyleableRes index: Int): Int? {
    val result = getResourceId(index, -1)
    return if (result == -1) null else result
}