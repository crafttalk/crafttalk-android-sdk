package com.crafttalk.sampleChat

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import com.crafttalk.chat.R as ChatR
import com.crafttalk.chat.utils.PhoneMaskUtils
import com.crafttalk.sampleChat.actions.waitUntilDisplayed
import com.crafttalk.sampleChat.auth.AuthActivity
import org.junit.Rule
import org.junit.Test

class ChatTest {
    @get:Rule val screen = ActivityTestRule(AuthActivity::class.java)

    @Test fun openingChatWithRegistrationFormatsTheVisitorsPhoneNumber() {
        onView(withId(R.id.link_to_channel_user)).perform(
            replaceText("http://127.0.0.1:10002/webchat/test"), closeSoftKeyboard()
        )
        onView(withId(R.id.switch_auth_with_form)).perform(click())
        onView(withId(R.id.sign_in_with_auth)).perform(click())

        onView(isRoot()).perform(waitUntilDisplayed(ChatR.id.auth_form))
        val countryCode = PhoneMaskUtils.getCountryCodeBySim(screen.activity)
        onView(withId(ChatR.id.phone_user))
            .perform(replaceText("$countryCode 12"), closeSoftKeyboard())
            .check(matches(withText("$countryCode (12")))
    }
}
