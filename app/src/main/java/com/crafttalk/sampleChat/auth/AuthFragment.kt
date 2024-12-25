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
import java.util.*
import com.crafttalk.sampleChat.databinding.FragmentAuthBinding

class AuthFragment: Fragment(R.layout.fragment_auth) {
    private var fragmentAuthBinding: FragmentAuthBinding? = null

    private val textListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            enableSignInWithAuth()
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun enableSignInWithAuth() {
        val binding = FragmentAuthBinding.bind(requireView())
        fragmentAuthBinding = binding

        binding.signInWithAuth.isEnabled = binding.switchAuthWithForm.isChecked ||
                (binding.uuidUser.text.isNotBlank() || binding.firstNameUser.text.isNotBlank() ||
                        binding.lastNameUser.text.isNotBlank() ||
                        binding.saltUser.text.isNotBlank()
                        )
    }

    private fun generateVisitor(isAnonymously: Boolean): Visitor? {
        val binding = FragmentAuthBinding.bind(requireView())
        fragmentAuthBinding = binding

        if (binding.switchAuthWithForm.isChecked) return null
        val uuid = binding.uuidUser.text.toString().ifEmpty { UUID.randomUUID().toString() }
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
            val firstName = binding.firstNameUser.text.toString()
            val lastName = binding.lastNameUser.text.toString()
            val salt = binding.saltUser.text.toString()
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
        val binding = FragmentAuthBinding.bind(requireView())
        fragmentAuthBinding = binding
        startActivity(
            Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra("key_is_auth_with_form", binding.switchAuthWithForm.isChecked)
                putExtra("key_visitor", generateVisitor(isAnonymously))
            }
        )
    }

    private var lastTypeAuth: TypeAuth? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAuthBinding.bind(view)
        fragmentAuthBinding = binding

        binding.uuidUser.addTextChangedListener(textListener)
        binding.firstNameUser.addTextChangedListener(textListener)
        binding.lastNameUser.addTextChangedListener(textListener)
        binding.saltUser.addTextChangedListener(textListener)
        binding.switchAuthWithForm.setOnClickListener {
            enableSignInWithAuth()
        }

        binding.signInWithAuth.setOnClickListener {
            if (lastTypeAuth != TypeAuth.CHAT_SIMPLE) {
                Chat.logOutWithUIActionAfter(requireContext()) {
                    lastTypeAuth = TypeAuth.CHAT_SIMPLE
                    openChat(false)
                }
            } else {
                openChat(false)
            }
        }
        binding.signInAnonymously.setOnClickListener {
            if (lastTypeAuth != TypeAuth.CHAT_ANONYMOUSLY) {
                Chat.logOutWithUIActionAfter(requireContext()) {
                    lastTypeAuth = TypeAuth.CHAT_ANONYMOUSLY
                    openChat(true)
                }
            } else {
                openChat(true)
            }
        }
        binding.signInChatWithCounter.setOnClickListener {
            if (lastTypeAuth != TypeAuth.CHAT_WITH_COUNTER) {
                Chat.logOutWithUIActionAfter(requireContext()) {
                    lastTypeAuth = TypeAuth.CHAT_WITH_COUNTER
                    startActivity(Intent(requireContext(), ChatWithCounterActivity::class.java))
                }
            } else {
                startActivity(Intent(requireContext(), ChatWithCounterActivity::class.java))
            }
        }
        binding.signInWebView.setOnClickListener {
            startActivity(Intent(requireContext(), WebViewActivity::class.java))
        }
    }
}