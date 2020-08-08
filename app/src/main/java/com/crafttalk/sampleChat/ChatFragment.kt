package com.crafttalk.sampleChat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_chat.*
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.presentation.ChatView

class ChatFragment: Fragment(R.layout.fragment_chat) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chat_view.onCreate(
            this,
            object:
                ChatView.EventListener {
                    override fun onErrorAuth() {}
                    override fun onAuth() {}
                },
            Visitor(
                "test_101",
                "user_1",
                "user_1",
                "test",
                "243",
                "wrrd",
                "17.09.1235"
            )
        )
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

    override fun onDestroyView() {
        super.onDestroyView()
    }


}