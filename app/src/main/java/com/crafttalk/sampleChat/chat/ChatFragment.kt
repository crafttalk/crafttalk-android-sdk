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

        Log.d("CTALK_TEST_DALO", "data 2: ${activity?.intent?.getBooleanExtra("key_is_auth_with_form", false)}; " +
                "${activity?.intent?.getBooleanExtra("key_is_auth_with_form", false) == true}" +
                "${activity?.intent?.getSerializableExtra("key_visitor")};")


        Chat.init(
            requireContext(),
            getString(R.string.urlChatScheme),
            getString(R.string.urlChatHost),
            getString(R.string.urlChatNameSpace),
            authType = (if (activity?.intent?.getBooleanExtra("key_is_auth_with_form", false) == true)
                AuthType.AUTH_WITH_FORM
            else
                AuthType.AUTH_WITHOUT_FORM).apply { Log.d("CTALK_TEST_DALO", "type: ${this};") },
            fileProviderAuthorities = getString(R.string.chat_file_provider_authorities)
        )
        Chat.createSession()
    }

    @SuppressLint("ClickableViewAccessibility")
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
//        chat_view.setOnInternetConnectionListener(object : ChatInternetConnectionListener {
//            override fun connect() { status_connection.visibility = View.GONE }
//            override fun failConnect() { status_connection.visibility = if (search_place.isVisible) View.GONE else View.VISIBLE }
//            override fun lossConnection() { status_connection.visibility = if (search_place.isVisible) View.GONE else View.VISIBLE }
//            override fun reconnect() { status_connection.visibility = View.GONE }
//        })
//        chat_view.setOnChatStateListener(object : ChatStateListener {
//            override fun startSynchronization() { chat_state.visibility = if (search_place.isVisible) View.GONE else View.VISIBLE }
//            override fun endSynchronization() { chat_state.visibility = View.GONE }
//        })
//        chat_view.setSearchListener(object : SearchListener {
//            override fun start() {
//                search_place.findViewById<EditText>(R.id.search_input).apply {
//                    setCompoundDrawablesWithIntrinsicBounds(
//                        ContextCompat.getDrawable(context, com.crafttalk.chat.R.drawable.com_crafttalk_chat_ic_hourglass),
//                        compoundDrawables[1],
//                        compoundDrawables[2],
//                        compoundDrawables[3]
//                    )
//                }
//            }
//            override fun stop() {
//                search_place.findViewById<EditText>(R.id.search_input).apply {
//                    setCompoundDrawablesWithIntrinsicBounds(
//                        ContextCompat.getDrawable(context, com.crafttalk.chat.R.drawable.com_crafttalk_chat_ic_search),
//                        compoundDrawables[1],
//                        compoundDrawables[2],
//                        compoundDrawables[3]
//                    )
//                }
//            }
//        })
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
//        search.setOnClickListener {
//            search.visibility = View.GONE
//            icon.visibility = View.GONE
//            chat_state.visibility = View.GONE
//            status_connection.visibility = View.GONE
//            search_place.visibility = View.VISIBLE
//            hideSoftKeyboard(chat_view)
//        }
//        search_place.findViewById<TextView>(R.id.search_cancel).setOnClickListener {
//            search_place.visibility = View.GONE
//            chat_state.visibility = View.GONE
//            status_connection.visibility = View.GONE
//            search.visibility = View.VISIBLE
//            icon.visibility = View.VISIBLE
//            search_place.findViewById<EditText>(R.id.search_input).text.clear()
//            chat_view.onSearchCancelClick()
//        }
//        search_place.findViewById<EditText>(R.id.search_input).apply {
//            setOnTouchListener { view, motionEvent ->
//                val drawableLeft = 0
//                val drawableRight = 2
//
//                if(motionEvent.action == MotionEvent.ACTION_UP) {
//                    if(motionEvent.x + left >= (right - compoundDrawables[drawableRight].bounds.width() - compoundDrawablePadding)) {
//                        text.clear()
//                    } else if (motionEvent.x < (paddingLeft + compoundDrawables[drawableLeft].bounds.width())) {
//                        chat_view.searchText(text.toString())
//                    }
//                }
//                false
//            }
//            auto search
//            addTextChangedListener(object : TextWatcher {
//                override fun afterTextChanged(s: Editable?) {
//                    chat_view.searchText(text.toString())
//                }
//                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//            })
//        }
    }

    override fun onResume() {
        super.onResume()
//        Log.d("TEST_DALO", "")

        Chat.wakeUp(
            (if (activity?.intent?.getBooleanExtra("key_is_auth_with_form", false) == true)
                null
            else
                (activity?.intent?.getSerializableExtra("key_visitor") as? Visitor)!!).apply {
                Log.d("CTALK_TEST_DALO", "getVisitor: ${this};")
            }
        )
        chat_view.onResume()
    }

    override fun onStop() {
        super.onStop()
        chat_view.onStop()
        Chat.drop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chat_view.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        Chat.destroySession()
    }

    private fun showWarning(warningText: String) {
        Snackbar.make(chat_view, warningText, Snackbar.LENGTH_LONG).show()
    }

}