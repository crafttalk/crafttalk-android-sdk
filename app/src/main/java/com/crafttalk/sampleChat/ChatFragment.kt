package com.crafttalk.sampleChat

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.crafttalk.chat.presentation.ChatInternetConnectionListener
import com.crafttalk.chat.presentation.ChatPermissionListener
import com.crafttalk.chat.presentation.ChatStateListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment: Fragment(R.layout.fragment_chat) {

    private var requestPermission: ActivityResultLauncher<String>? = null
    private var callbackResult: (isGranted: Boolean) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            callbackResult(isGranted)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chat_view.onViewCreated(this, viewLifecycleOwner)
        chat_view.setOnInternetConnectionListener(object : ChatInternetConnectionListener {
            override fun connect() { status_connection.visibility = View.GONE }
            override fun failConnect() { status_connection.visibility = View.VISIBLE }
            override fun lossConnection() { status_connection.visibility = View.VISIBLE }
            override fun reconnect() { status_connection.visibility = View.GONE }
        })
        chat_view.setOnChatStateListener(object : ChatStateListener {
            override fun startSynchronization() { chat_state.visibility = View.VISIBLE }
            override fun endSynchronization() { chat_state.visibility = View.GONE }
        })
        chat_view.setOnPermissionListener(object : ChatPermissionListener {
            override fun requestedPermissions(permissions: Array<String>, messages: Array<String>, action: () -> Unit) {
                callbackResult = { isGranted ->
                    if (isGranted) {
                        action()
                    } else {
                        showWarning(messages[0])
                    }
                }
                requestPermission?.launch(permissions[0])
            }
        })
    }

    override fun onStart() {
        super.onStart()
        chat_view.onStart()
    }

    private fun showWarning(warningText: String) {
        Snackbar.make(chat_view, warningText, Snackbar.LENGTH_LONG).show()
    }

}