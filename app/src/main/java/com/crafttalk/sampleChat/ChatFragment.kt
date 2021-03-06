package com.crafttalk.sampleChat

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.crafttalk.chat.presentation.ChatInternetConnectionListener
import com.crafttalk.chat.presentation.ChatPermissionListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment: Fragment(R.layout.fragment_chat) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chat_view.onCreate(this, viewLifecycleOwner)
        chat_view.setOnPermissionListener(object : ChatPermissionListener {
            override fun requestedPermissions(permissions: Array<String>, messages: Array<String>, action: () -> Unit) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                    if (isGranted) {
                        action()
                    } else {
                        showWarning(messages[0])
                    }
                }.launch(permissions[0])
            }
        })
        chat_view.setOnInternetConnectionListener(object : ChatInternetConnectionListener {
            override fun connect() { status_connection.visibility = View.GONE }
            override fun failConnect() { status_connection.visibility = View.VISIBLE }
            override fun lossConnection() { status_connection.visibility = View.VISIBLE }
            override fun reconnect() { status_connection.visibility = View.GONE }
        })
    }

    override fun onResume() {
        super.onResume()
        chat_view.onResume(viewLifecycleOwner)
    }

    private fun showWarning(warningText: String) {
        Snackbar.make(chat_view, warningText, Snackbar.LENGTH_LONG).show()
    }

}