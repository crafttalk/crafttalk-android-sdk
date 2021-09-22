package com.crafttalk.sampleChat

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.crafttalk.chat.initialization.Chat
import com.crafttalk.chat.initialization.ChatMessageListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity: AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = object: BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            val fragmentNow = supportFragmentManager.findFragmentById(R.id.fl_content)

            when (item.itemId) {
                R.id.navigation_home -> {
                    if (fragmentNow !is HomeFragment) {
                        loadFragment(HomeFragment())
                    }
                    return true
                }
                R.id.navigation_chat -> {
                    if (fragmentNow !is ChatFragment) {
                        loadFragment(ChatFragment())
                    }
                    return true
                }
                R.id.navigation_settings -> {
                    if (fragmentNow !is SettingsFragment) {
                        loadFragment(SettingsFragment())
                    }
                    return true
                }
            }
            return false
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fl_content, fragment)
        ft.commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        Chat.init(
            this,
            getString(R.string.urlChatScheme),
            getString(R.string.urlChatHost),
            getString(R.string.urlChatNameSpace),
            fileProviderAuthorities = getString(R.string.chat_file_provider_authorities)
        )
        Chat.createSession()
        Chat.setOnChatMessageListener(object : ChatMessageListener {
            override fun getNewMessages(countMessages: Int) {
                Log.d("TEST_GET_MSG", "get new messages count - ${countMessages};")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Chat.wakeUp(getVisitor(this))
    }

    override fun onStop() {
        super.onStop()
        Chat.drop()
    }

    override fun onDestroy() {
        super.onDestroy()
        Chat.destroySession()
    }

}