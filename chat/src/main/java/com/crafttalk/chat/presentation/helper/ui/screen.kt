package com.crafttalk.chat.presentation.helper.ui

import android.content.Context
import android.graphics.Point
import android.view.WindowManager

fun getSizeScreenInPx(context: Context): Pair<Int, Int> {
    val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    val size = Point()
    display.getSize(size)
    return Pair(size.x, size.y)
}