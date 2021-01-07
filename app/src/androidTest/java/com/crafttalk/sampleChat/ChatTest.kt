package com.crafttalk.sampleChat

import android.util.Log
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.crafttalk.sampleChat.actions.*
import org.hamcrest.CoreMatchers.anyOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testOpenAttachSpace() {
        onView(anyOf(withId(R.id.navigation_chat))).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.camera)).check(doesNotExist())
        onView(withId(R.id.entry_field)).perform(clearText())
        onView(withId(R.id.send_message)).perform(click())
        Log.d("TEST_LIST", "test")
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.entry_field)).perform(typeText("sfbsdif dif sdiufdh fygfd uofygfuwg wyfdfidohfuoufdifd fid ifd id sdfd fd "))
        onView(withId(R.id.camera)).check(doesNotExist())//.check(matches(isDisplayed()))
    }

    @Test
    fun testMessageFieldHasFocus() {
        onView(anyOf(withId(R.id.navigation_chat))).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.entry_field)).perform(typeText(""))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.entry_field)).perform(click()).check(matches(hasFocus()))
    }

    @Test
    fun testSendEmptyMessage() {
        onView(anyOf(withId(R.id.navigation_chat))).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.entry_field)).perform(typeText(""))
        onView(withId(R.id.send_message)).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.entry_field)).check(matches(withText("")))
    }

    @Test
    fun testSendSpaceMessage() {
        onView(anyOf(withId(R.id.navigation_chat))).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.entry_field)).perform(typeText(" "))
        onView(withId(R.id.send_message)).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.entry_field)).check(matches(withText("")))
    }

    @Test
    fun testSendLineTranslationMessage() {
        onView(anyOf(withId(R.id.navigation_chat))).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.entry_field)).perform(typeText("\n"))
        onView(withId(R.id.send_message)).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.entry_field)).check(matches(withText("")))
    }

    @Test
    fun testSendTextMessage() {
        onView(anyOf(withId(R.id.navigation_chat))).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.entry_field)).perform(clearText())
        onView(withId(R.id.entry_field)).perform(typeText("test"))
        onView(withId(R.id.send_message)).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.list_with_message)).perform(ScrollToEndMessagesViewAction())
        onView(withId(R.id.entry_field)).check(matches(withText("")))
        onView(withId(R.id.user_message)).check(matches(withText("test")))
    }

    @Test
    fun testSendEmojiMessage() {
        onView(anyOf(withId(R.id.navigation_chat))).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.entry_field)).perform(replaceText("\uD83D\uDE08"))
        onView(withId(R.id.send_message)).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.list_with_message)).perform(ScrollToEndMessagesViewAction())
        onView(withId(R.id.entry_field)).check(matches(withText("")))
        onView(withId(R.id.user_message)).check(matches(withText("\uD83D\uDE08")))
    }

    @Test
    fun testPickActionMessage() {
        onView(anyOf(withId(R.id.navigation_chat))).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.entry_field)).perform(clearText())
        onView(withId(R.id.list_with_message)).perform(ScrollToStartMessagesViewAction())
        onView(withId(R.id.actions_list)).perform(
            ClickActionOnItemAtPositionInStartMessageViewAction()
        )
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.entry_field)).check(matches(withText("")))
    }

    @Test
    fun openTextFromMessage() {
        onView(anyOf(withId(R.id.navigation_chat))).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.image_show)).check(doesNotExist())
        onView(withId(R.id.list_with_message)).perform(ClickTextMessageViewAction())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.image_show)).check(doesNotExist())
    }

    @Test
    fun openImageFromMessage() {
        onView(anyOf(withId(R.id.navigation_chat))).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.image_show)).check(doesNotExist())
        onView(withId(R.id.list_with_message)).perform(ClickImageMessageViewAction())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.image_show)).check(matches(isDisplayed()))
    }

    @Test
    fun openGifFromMessage() {
        onView(anyOf(withId(R.id.navigation_chat))).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.image_show)).check(doesNotExist())
        onView(withId(R.id.list_with_message)).perform(ClickGifMessageViewAction())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.image_show)).check(matches(isDisplayed()))
    }

    @Test
    fun openFileFromMessage() {
        onView(anyOf(withId(R.id.navigation_chat))).perform(click())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.image_show)).check(doesNotExist())
        onView(withId(R.id.list_with_message)).perform(ClickFileMessageViewAction())
        onView(isRoot()).perform(waitFor(5000))
        onView(withId(R.id.image_show)).check(doesNotExist())
    }

}