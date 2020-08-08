package com.crafttalk.chat.presentation.helper.converters

import android.content.Context
import android.util.TypedValue

fun convertDpToPx(dp: Float, context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        context.resources.displayMetrics
    )
}