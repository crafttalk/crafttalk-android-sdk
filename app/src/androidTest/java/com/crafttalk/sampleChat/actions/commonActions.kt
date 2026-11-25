package com.crafttalk.sampleChat.actions

import android.os.SystemClock
import android.view.View
import androidx.annotation.IdRes
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot

fun waitUntilDisplayed(@IdRes id: Int) = object : ViewAction {
    override fun getConstraints() = isRoot()
    override fun getDescription() = "wait for view $id to be displayed"

    override fun perform(uiController: UiController, root: View) {
        val deadline = SystemClock.uptimeMillis() + 10_000
        val displayed = isDisplayed()
        do {
            val view = root.findViewById<View>(id)
            if (view != null && displayed.matches(view)) return
            uiController.loopMainThreadForAtLeast(50)
        } while (SystemClock.uptimeMillis() < deadline)
        throw AssertionError("View ${root.resources.getResourceName(id)} was not displayed within 10 seconds")
    }
}
