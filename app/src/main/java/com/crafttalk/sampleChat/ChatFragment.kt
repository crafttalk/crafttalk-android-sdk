package com.crafttalk.sampleChat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_chat.*
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.presentation.ChatView
import com.crafttalk.chat.utils.HashUtils

class ChatFragment: Fragment(R.layout.fragment_chat) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val salt = getString(R.string.salt)

        chat_view.onCreate(
            this,
            object:
                ChatView.EventListener {
                    override fun onErrorAuth() {}
                    override fun onAuth() {}
                },
            Visitor(
                uuid,
                firstName,
                lastName,
                email,
                phone,
                contract,
                birthday,
                HashUtils.getHash("SHA-256", "${salt}${HashUtils.getHash("SHA-256", "${salt}${source}")}")
            )
        )
    }

    override fun onResume() {
        super.onResume()
        chat_view.onResume(this)
    }

    companion object {
        const val uuid = "test_104"
        const val firstName = "Ivan"
        const val lastName = "Ivanov"
        const val email = "email"
        const val phone = "000000000"
        const val contract = "contract_test"
        const val birthday = "00.00.00"
        const val source = "${uuid}${firstName}${lastName}${contract}${phone}${email}${birthday}"
    }
}