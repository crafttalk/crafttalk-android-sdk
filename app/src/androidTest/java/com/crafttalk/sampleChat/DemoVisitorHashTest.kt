package com.crafttalk.sampleChat

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.sampleChat.auth.AuthActivity
import com.crafttalk.sampleChat.chat.ChatFragment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class DemoVisitorHashTest {
    @get:Rule val screen = ActivityTestRule(AuthActivity::class.java)

    @Test fun signedVisitorFromLoginButton() {
        // Server signature for demo-33, Test, User and four absent optional fields.
        assertEquals("b7e2759ba6eb25df97141875a82625b107b819a674db8f725b034288642bb48e",
            signIn("demo-salt").hash)
    }

    @Test fun unsignedVisitorFromLoginButton() {
        assertNull(signIn("").hash)
    }

    private fun signIn(salt: String): Visitor {
        val opened = LinkedBlockingQueue<Visitor>()
        screen.runOnUiThread {
            screen.activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
                        if (f is ChatFragment) {
                            val visitor = f.requireArguments().getSerializable("key_visitor") as Visitor
                            opened.add(visitor)
                        }
                    }
                }, false
            )
        }
        onView(withId(R.id.uuid_user)).perform(replaceText("demo-33"))
        onView(withId(R.id.first_name_user)).perform(replaceText("Test"))
        onView(withId(R.id.last_name_user)).perform(replaceText("User"))
        onView(withId(R.id.salt_user)).perform(replaceText(salt))
        onView(withId(R.id.link_to_channel_user)).perform(replaceText("http://127.0.0.1/webchat/test"), closeSoftKeyboard())
        onView(withId(R.id.sign_in_with_auth)).perform(click())
        return requireNotNull(opened.poll(10, TimeUnit.SECONDS)) {
            "The login button must open the chat with a Visitor"
        }
    }
}
