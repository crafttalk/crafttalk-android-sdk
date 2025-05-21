package com.crafttalk.sampleChat.actions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.crafttalk.chat.presentation.adapters.AdapterListMessages
import com.crafttalk.chat.presentation.holders.HolderAction
import com.crafttalk.chat.presentation.model.FileMessageItem
import com.crafttalk.chat.presentation.model.GifMessageItem
import com.crafttalk.chat.presentation.model.ImageMessageItem
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher

class ScrollToEndMessagesViewAction : ViewAction {
    override fun getDescription(): String {
        return "scroll RecyclerView to end"
    }

    override fun getConstraints(): Matcher<View> {
        return allOf<View>(isAssignableFrom(RecyclerView::class.java), isDisplayed())
    }

    override fun perform(uiController: UiController?, view: View?) {
        val recyclerView = view as RecyclerView
        val itemCount = recyclerView.adapter?.itemCount
        val position = itemCount?.minus(1) ?: 0
        recyclerView.scrollToPosition(position)
        uiController?.loopMainThreadUntilIdle()
    }
}

class ScrollToStartMessagesViewAction : ViewAction {
    override fun getDescription(): String {
        return "scroll RecyclerView to start"
    }

    override fun getConstraints(): Matcher<View> {
        return allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())
    }

    override fun perform(uiController: UiController?, view: View?) {
        val recyclerView = view as RecyclerView
        recyclerView.scrollToPosition(0)
        uiController?.loopMainThreadUntilIdle()
    }
}

class ClickActionOnItemAtPositionInStartMessageViewAction : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())
    }

    override fun getDescription(): String {
        return "click on action"
    }

    override fun perform(uiController: UiController?, view: View?) {
        val recyclerView = view as RecyclerView
        val itemView = (recyclerView.findViewHolderForAdapterPosition(0) as HolderAction).itemView
        click().perform(uiController, itemView)
        uiController?.loopMainThreadUntilIdle()
    }

}

class ClickTextMessageViewAction : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())
    }

    override fun getDescription(): String {
        return "click on text message"
    }

    override fun perform(uiController: UiController?, view: View?) {
        val recyclerView = view as RecyclerView
        val adapter = recyclerView.adapter as AdapterListMessages
        for(index in 0 until (recyclerView.adapter?.itemCount ?: 0)) {
            val item = adapter.getItem(index)
            when (item) {
                is ImageMessageItem -> {
                    recyclerView.scrollToPosition(index)
                    click().perform(uiController, recyclerView.findViewHolderForAdapterPosition(index)!!.itemView)
                    uiController?.loopMainThreadUntilIdle()
                    return
                }
            }
        }
    }

}

class ClickImageMessageViewAction : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())
    }

    override fun getDescription(): String {
        return "click on image message"
    }

    override fun perform(uiController: UiController?, view: View?) {
        val recyclerView = view as RecyclerView
        val adapter = recyclerView.adapter as AdapterListMessages
        for(index in 0 until (recyclerView.adapter?.itemCount ?: 0)) {
            val item = adapter.getItem(index)
            when (item) {
                is ImageMessageItem -> {
                    recyclerView.scrollToPosition(index)
                    click().perform(uiController, recyclerView.findViewHolderForAdapterPosition(index)!!.itemView)
                    uiController?.loopMainThreadUntilIdle()
                    return
                }
            }
        }
    }

}

class ClickGifMessageViewAction : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())
    }

    override fun getDescription(): String {
        return "click on gif message"
    }

    override fun perform(uiController: UiController?, view: View?) {
        val recyclerView = view as RecyclerView
        val adapter = recyclerView.adapter as AdapterListMessages
        for(index in 0 until (recyclerView.adapter?.itemCount ?: 0)) {
            val item = adapter.getItem(index)
            when (item) {
                is GifMessageItem -> {
                    recyclerView.scrollToPosition(index)
                    click().perform(uiController, recyclerView.findViewHolderForAdapterPosition(index)!!.itemView)
                    uiController?.loopMainThreadUntilIdle()
                    return
                }
            }
        }
    }

}

class ClickFileMessageViewAction : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return allOf(isAssignableFrom(RecyclerView::class.java), isDisplayed())
    }

    override fun getDescription(): String {
        return "click on file message"
    }

    override fun perform(uiController: UiController?, view: View?) {
        val recyclerView = view as RecyclerView
        val adapter = recyclerView.adapter as AdapterListMessages
        for(index in 0 until (recyclerView.adapter?.itemCount ?: 0)) {
            val item = adapter.getItem(index)
            when (item) {
                is FileMessageItem -> {
                    recyclerView.scrollToPosition(index)
                    click().perform(uiController, recyclerView.findViewHolderForAdapterPosition(index)!!.itemView)
                    uiController?.loopMainThreadUntilIdle()
                    return
                }
            }
        }
    }

}