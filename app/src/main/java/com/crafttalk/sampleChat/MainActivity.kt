package com.crafttalk.sampleChat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity: AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = object:
        BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.navigation_home -> {
                        loadFragment(HomeFragment())
                        return true
                    }
                    R.id.navigation_chat -> {
                        loadFragment(ChatFragment())
                        return true
                    }
                    R.id.navigation_settings -> {
                        loadFragment(SettingsFragment())
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
