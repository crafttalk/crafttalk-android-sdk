package com.crafttalk.sampleChat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.crafttalk.chat.ui.ListenerChat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        chat_view.onCreate(application, object: ListenerChat {
            override fun onErrorAuth() {}
            override fun onAuth() {}
        })
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        chat_view.onResume(this)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        chat_view.onDestroy()
        super.onDestroy()
    }
}
