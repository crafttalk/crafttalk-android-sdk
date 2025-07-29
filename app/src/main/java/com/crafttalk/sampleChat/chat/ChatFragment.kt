package com.crafttalk.sampleChat.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.crafttalk.chat.domain.entity.auth.Visitor
import com.crafttalk.chat.initialization.Chat
import com.crafttalk.chat.presentation.ChatPermissionListener
import com.crafttalk.chat.utils.AuthType
import com.crafttalk.sampleChat.R
import com.crafttalk.sampleChat.databinding.FragmentChatBinding
import com.crafttalk.sampleChat.widgets.carousel.CarouselWidget
import com.crafttalk.sampleChat.widgets.carousel.bindCarouselWidget
import com.crafttalk.sampleChat.widgets.carousel.createCarouselWidget
import com.google.android.material.snackbar.Snackbar

class ChatFragment: Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private var requestPermission: ActivityResultLauncher<String>? = null
    private var callbackResult: (isGranted: Boolean) -> Unit = {}
    private val inflater: LayoutInflater by lazy { LayoutInflater.from(context) }

    private var isAuthWithForm: Boolean = false
    private var visitor: Visitor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            isAuthWithForm = bundle.getBoolean("key_is_auth_with_form", false)
            visitor = bundle.getSerializable("key_visitor") as? Visitor
        }

        requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            callbackResult(isGranted)
        }

        Chat.init(
            requireContext(),
            getString(R.string.urlChatScheme),
            getString(R.string.urlChatHost),
            getString(R.string.urlChatNameSpace),
            authType = if (isAuthWithForm) AuthType.AUTH_WITH_FORM else AuthType.AUTH_WITHOUT_FORM
                .also { Log.d("CTALK_TEST_DALO", "type: $it;") },
            fileProviderAuthorities = getString(R.string.chat_file_provider_authorities)
        )
        Chat.createSession()
        Chat.clearDBDialogHistory(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.chatView.visibility = View.VISIBLE
        binding.chatView.setMethodGetPayloadTypeWidget { widgetId ->
            when (widgetId) {
                "carousel" -> CarouselWidget::class.java
                else -> null
            }
        }
        binding.chatView.setMethodGetWidgetView { widgetId ->
            when (widgetId) {
                "carousel" -> createCarouselWidget(inflater)
                else -> null
            }
        }
        binding.chatView.setMethodFindItemsViewOnWidget { widgetId, widgetView, mapView ->
            when (widgetId) {
                "carousel" -> {
                    mapView["list_carousel"] = widgetView.findViewById<ViewGroup>(R.id.list_carousel)
                }
            }
        }
        binding.chatView.setMethodBindWidget { widgetId, message, mapView, payload ->
            when (widgetId) {
                "carousel" -> {
                    val data = (payload as? CarouselWidget) ?: return@setMethodBindWidget
                    val listView = (mapView["list_carousel"] as? ViewGroup) ?: return@setMethodBindWidget
                    bindCarouselWidget(inflater, listView, data, binding.chatView::clickButtonInWidget)
                }
            }
        }

        binding.chatView.onViewCreated(this, viewLifecycleOwner)
        binding.chatView.setOnPermissionListener(object : ChatPermissionListener {
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
        binding.chatView.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()

        if (isAuthWithForm) {
            Chat.wakeUp(null)
        } else {
            if (visitor != null) {
                Chat.wakeUp(visitor)
            }
        }

        binding.chatView.onResume(visitor)
    }

    override fun onStop() {
        super.onStop()
        binding.chatView.onStop()
        Chat.drop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Chat.destroySession()
    }

    private fun showWarning(warningText: String) {
        Snackbar.make(binding.chatView, warningText, Snackbar.LENGTH_LONG).show()
    }
}