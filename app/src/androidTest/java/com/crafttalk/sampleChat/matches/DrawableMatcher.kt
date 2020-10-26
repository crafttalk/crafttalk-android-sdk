package com.crafttalk.sampleChat.matches

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView

import androidx.annotation.DrawableRes
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher


class DrawableMatcher(
    @param:DrawableRes private val resourceId: Int
) : TypeSafeMatcher<View?>(View::class.java) {
    private var resourceName: String? = null

    override fun matchesSafely(target: View?): Boolean {
        if (target !is ImageView) {
            return false
        }
        val imageView: ImageView = target
        if (resourceId < 0) {
            return imageView.drawable == null
        }
        val resources: Resources = target.getContext().resources
        val expectedDrawable: Drawable = resources.getDrawable(resourceId)
        resourceName = resources.getResourceEntryName(resourceId)
        return if (expectedDrawable.constantState != null) {
            expectedDrawable.constantState == imageView.drawable.constantState
        } else {
            false
        }
    }

    override fun describeTo(description: Description) {
        description.appendText("with drawable from resource id: ")
        description.appendValue(resourceId)
        if (resourceName != null) {
            description.appendText("[")
            description.appendText(resourceName)
            description.appendText("]")
        }
    }

}