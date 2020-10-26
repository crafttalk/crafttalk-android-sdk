package com.crafttalk.sampleChat.matches

import android.view.View
import androidx.annotation.DrawableRes
import org.hamcrest.Matcher


fun withDrawableId(@DrawableRes id: Int): Matcher<View?>? {
    return DrawableMatcher(id)
}