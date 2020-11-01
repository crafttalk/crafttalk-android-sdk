package com.crafttalk.sampleChat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.initialization.ChatPermissionListener
import com.crafttalk.chat.utils.HashUtils
import com.crafttalk.chat.utils.R_PERMISSIONS
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment: Fragment(R.layout.fragment_chat) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val salt = getString(R.string.salt)

        chat_view.onCreate(
            this,
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
        chat_view.setOnPermissionListener(object : ChatPermissionListener {
            override fun requestedPermissions(permissions: Array<R_PERMISSIONS>, messages: Array<String>) {
                permissions.forEachIndexed { index, permission ->
                    showWarning(messages[index])
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        chat_view.onResume(this)
    }


    private fun showWarning(warningText: String) {
        Snackbar.make(chat_view, warningText, Snackbar.LENGTH_LONG).show()
    }

    companion object {
        const val uuid = "Karl23"
        const val firstName = "Karl23"
        const val lastName = "Testovich_Ivanovich"
        const val email = "email"
        const val phone = "000000000"
        const val contract = "contract_test"
        const val birthday = "00.00.00"
        const val source = "${uuid}${firstName}${lastName}${contract}${phone}${email}${birthday}"
    }
}