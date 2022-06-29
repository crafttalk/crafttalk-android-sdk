package com.crafttalk.sampleChat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.crafttalk.chat.presentation.ChatInternetConnectionListener
import com.crafttalk.chat.presentation.ChatPermissionListener
import com.crafttalk.chat.presentation.ChatStateListener
import com.crafttalk.sampleChat.widgets.carousel.CarouselWidget
import com.crafttalk.sampleChat.widgets.carousel.bindCarouselWidget
import com.crafttalk.sampleChat.widgets.carousel.createCarouselWidget
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment: Fragment(R.layout.fragment_chat) {

    private var requestPermission: ActivityResultLauncher<String>? = null
    private var callbackResult: (isGranted: Boolean) -> Unit = {}
    private val inflater: LayoutInflater by lazy { LayoutInflater.from(context) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            callbackResult(isGranted)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chat_view.setMethodGetPayloadTypeWidget { widgetId ->
            when (widgetId) {
                "carousel" -> CarouselWidget::class.java
                else -> null
            }
        }
        chat_view.setMethodGetWidgetView { widgetId ->
            when (widgetId) {
                "carousel" -> createCarouselWidget(inflater)
                else -> null
            }
        }
        chat_view.setMethodFindItemsViewOnWidget { widgetId, widgetView, mapView ->
            when (widgetId) {
                "carousel" -> {
                    mapView["list_carousel"] = widgetView.findViewById<ViewGroup>(R.id.list_carousel)
                }
            }
        }
        chat_view.setMethodBindWidget { widgetId, message, mapView, payload ->
            when (widgetId) {
                "carousel" -> {
                    val data = (payload as? CarouselWidget) ?: return@setMethodBindWidget
                    val listView = (mapView["list_carousel"] as? ViewGroup) ?: return@setMethodBindWidget
                    bindCarouselWidget(inflater, listView, data, chat_view::clickButtonInWidget)
                }
            }
        }

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

    override fun onResume() {
        super.onResume()
        chat_view.onResume()
    }

    override fun onStop() {
        super.onStop()
        chat_view.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chat_view.onDestroyView()
    }

    private fun showWarning(warningText: String) {
        Snackbar.make(chat_view, warningText, Snackbar.LENGTH_LONG).show()
    }

}