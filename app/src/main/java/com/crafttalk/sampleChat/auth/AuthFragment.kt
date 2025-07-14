package com.crafttalk.sampleChat.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.initialization.Chat
import com.crafttalk.chat.utils.HashUtils
import com.crafttalk.sampleChat.chat.ChatActivity
import com.crafttalk.sampleChat.chat_with_counter.ChatActivity as ChatWithCounterActivity
import com.crafttalk.sampleChat.R
import com.crafttalk.sampleChat.web_view.WebViewActivity
import kotlinx.android.synthetic.main.fragment_auth.*
import java.util.*

class AuthFragment: Fragment(R.layout.fragment_auth) {

    private val textListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            enableSignInWithAuth()
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun enableSignInWithAuth() {
        sign_in_with_auth.isEnabled = switch_auth_with_form.isChecked ||
                (uuid_user.text.isNotBlank() || first_name_user.text.isNotBlank() || last_name_user.text.isNotBlank() || salt_user.text.isNotBlank())
    }

    private fun generateVisitor(isAnonymously: Boolean): Visitor? {
        if (switch_auth_with_form.isChecked) return null
        val uuid = uuid_user.text.toString().ifEmpty { UUID.randomUUID().toString() }
        if (isAnonymously) {
            return Visitor(
                uuid = uuid,
                token = uuid,
                firstName = "",
                lastName = "",
                email = null,
                phone = null,
                contract = null,
                birthday = null,
                hash = null
            )
        } else {
            val firstName = first_name_user.text.toString()
            val lastName = last_name_user.text.toString()
            val salt = salt_user.text.toString()
            val source = "${uuid}${firstName}${lastName}${null}${null}${null}${null}"
            return Visitor(
                uuid = uuid,
                token = uuid,
                firstName = firstName,
                lastName = lastName,
                email = null,
                phone = null,
                contract = null,
                birthday = null,
                hash = if (salt.isBlank()) {
                    HashUtils.getHash(
                        "SHA-256",
                        "${salt}${HashUtils.getHash("SHA-256", "${salt}${source}")}"
                    )
                } else {
                    null
                }
            )
        }
    }

    private fun openChat(isAnonymously: Boolean) {
        startActivity(
            Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra("key_is_auth_with_form", switch_auth_with_form.isChecked)
                putExtra("key_visitor", generateVisitor(isAnonymously))
            }
        )
    }

    private var lastTypeAuth: TypeAuth? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uuid_user.addTextChangedListener(textListener)
        first_name_user.addTextChangedListener(textListener)
        last_name_user.addTextChangedListener(textListener)
        salt_user.addTextChangedListener(textListener)
        switch_auth_with_form.setOnClickListener {
            enableSignInWithAuth()
        }
        sign_in_with_auth.setOnClickListener {
            if (lastTypeAuth != TypeAuth.CHAT_SIMPLE) {
                Chat.logOutWithUIActionAfter(requireContext()) {
                    lastTypeAuth = TypeAuth.CHAT_SIMPLE
                    openChat(false)
                }
            } else {
                openChat(false)
            }
        }
        sign_in_anonymously.setOnClickListener {
            if (lastTypeAuth != TypeAuth.CHAT_ANONYMOUSLY) {
                Chat.logOutWithUIActionAfter(requireContext()) {
                    lastTypeAuth = TypeAuth.CHAT_ANONYMOUSLY
                    openChat(true)
                }
            } else {
                openChat(true)
            }
        }
        sign_in_chat_with_counter.setOnClickListener {
            if (lastTypeAuth != TypeAuth.CHAT_WITH_COUNTER) {
                Chat.logOutWithUIActionAfter(requireContext()) {
                    lastTypeAuth = TypeAuth.CHAT_WITH_COUNTER
                    startActivity(Intent(requireContext(), ChatWithCounterActivity::class.java))
                }
            } else {
                startActivity(Intent(requireContext(), ChatWithCounterActivity::class.java))
            }
        }
        sign_in_web_view.setOnClickListener {
            startActivity(Intent(requireContext(), WebViewActivity::class.java))
        }
    }
}