package com.crafttalk.sampleChat.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.initialization.Chat
import com.crafttalk.chat.utils.HashUtils
import com.crafttalk.sampleChat.R
import com.crafttalk.sampleChat.chat.ChatFragment
import com.crafttalk.sampleChat.chat_with_counter.ChatWithCounterActivity
import com.crafttalk.sampleChat.databinding.FragmentAuthBinding
import com.crafttalk.sampleChat.web_view.WebViewActivity
import java.util.*

class AuthFragment: Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val textListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            enableSignInWithAuth()
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun hasVisitorInput() = binding.uuidUser.text.isNotBlank() || binding.firstNameUser.text.isNotBlank() ||
            binding.lastNameUser.text.isNotBlank() || binding.saltUser.text.isNotBlank()

    private fun enableSignInWithAuth() {
        binding.signInWithAuth.isEnabled = binding.switchAuthWithForm.isChecked || hasVisitorInput()
    }

    private fun generateVisitor(isAnonymously: Boolean): Visitor? {
        if (binding.switchAuthWithForm.isChecked) return null
        val uuid = binding.uuidUser.text.toString().ifEmpty {
            UUID.randomUUID().toString().also { if (!isAnonymously) binding.uuidUser.setText(it) }
        }
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
            val source = "$uuid$firstName$lastName"
            return Visitor(
                uuid = uuid,
                token = uuid,
                firstName = firstName,
                lastName = lastName,
                email = null,
                phone = null,
                contract = null,
                birthday = null,
                hash = if (salt.isNotBlank()) {
                    val innerHash = HashUtils.getHash("SHA-256", salt + source)
                    HashUtils.getHash("SHA-256", salt + innerHash)
                } else {
                    null
                }
            )
        }
    }

    private fun chatArguments(isAnonymously: Boolean) = Bundle().apply {
        putBoolean("key_is_auth_with_form", binding.switchAuthWithForm.isChecked)
        putSerializable("key_visitor", generateVisitor(isAnonymously))
        putString("link_to_chat", binding.linkToChannelUser.text.toString())
    }

    private fun openCounterChat() {
        val intent = Intent(requireContext(), ChatWithCounterActivity::class.java)
        if (binding.switchAuthWithForm.isChecked || hasVisitorInput() || binding.linkToChannelUser.text.isNotBlank()) {
            intent.putExtras(chatArguments(false))
        }
        startActivity(intent)
    }

    private fun openChat(isAnonymously: Boolean) {
        val chatFragment = ChatFragment().apply { arguments = chatArguments(isAnonymously) }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, chatFragment)
            .addToBackStack(null)
            .commit()
    }


    private var lastTypeAuth: TypeAuth? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.uuidUser.addTextChangedListener(textListener)
        binding.firstNameUser.addTextChangedListener(textListener)
        binding.lastNameUser.addTextChangedListener(textListener)
        binding.saltUser.addTextChangedListener(textListener)
        binding.linkToChannelUser.addTextChangedListener(textListener)
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
                    openCounterChat()
                }
            } else {
                openCounterChat()
            }
        }
        binding.signInWebView.setOnClickListener {
            startActivity(Intent(requireContext(), WebViewActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
