package com.crafttalk.sampleChat

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity: AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = object:
        BottomNavigationView.OnNavigationItemSelectedListener {
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
        loadFragment(HomeFragment())
    }

}
