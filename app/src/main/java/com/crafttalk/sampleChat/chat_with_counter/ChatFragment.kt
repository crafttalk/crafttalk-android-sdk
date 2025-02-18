package com.crafttalk.sampleChat.chat_with_counter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.crafttalk.chat.domain.entity.file.File
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.initialization.Chat
import com.crafttalk.chat.presentation.ChatPermissionListener
import com.crafttalk.sampleChat.R
import com.crafttalk.sampleChat.databinding.FragmentChatBinding
import com.crafttalk.sampleChat.widgets.carousel.CarouselWidget
import com.crafttalk.sampleChat.widgets.carousel.bindCarouselWidget
import com.crafttalk.sampleChat.widgets.carousel.createCarouselWidget
import com.google.android.material.snackbar.Snackbar

class ChatFragment: Fragment(R.layout.fragment_chat) {
    private var fragmentChatBinding: FragmentChatBinding? = null

    private var requestPermission: ActivityResultLauncher<String>? = null
    private var callbackResult: (isGranted: Boolean) -> Unit = {}
    private val inflater: LayoutInflater by lazy { LayoutInflater.from(context) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            callbackResult(isGranted)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentChatBinding.inflate(inflater, container, false)
        fragmentChatBinding = binding
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentChatBinding.bind(view)
        fragmentChatBinding = binding
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
        binding.chatView.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        val binding = FragmentChatBinding.bind(requireView())
        fragmentChatBinding = binding
        binding.chatView.visibility = View.VISIBLE
        binding.chatView.onResume()
    }

    override fun onStop() {
        super.onStop()
        val binding = FragmentChatBinding.bind(requireView())
        fragmentChatBinding = binding
        binding.chatView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Chat.clearDBDialogHistory(requireContext())
        val binding = FragmentChatBinding.bind(requireView())
        fragmentChatBinding = binding

        binding.chatView.onDestroyView()
    }

    private fun showWarning(warningText: String) {
        val binding = FragmentChatBinding.bind(requireView())
        fragmentChatBinding = binding

        Snackbar.make(binding.chatView, warningText, Snackbar.LENGTH_LONG).show()
    }
    private val OPEN_DOCUMENT_REQUEST_CODE = 0x33
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        val binding = FragmentChatBinding.bind(requireView())
        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { documentUri ->

                /**
                 * Upon getting a document uri returned, we can use
                 * [ContentResolver.takePersistableUriPermission] in order to persist the
                 * permission across restarts.
                 *
                 * This may not be necessary for your app. If the permission is not
                 * persisted, access to the uri is granted until the receiving Activity is
                 * finished. You can extend the lifetime of the permission grant by passing
                 * it along to another Android component. This is done by including the uri
                 * in the data field or the ClipData object of the Intent used to launch that
                 * component. Additionally, you need to add FLAG_GRANT_READ_URI_PERMISSION
                 * and/or FLAG_GRANT_WRITE_URI_PERMISSION to the Intent.
                 *
                 * This app takes the persistable URI permission grant to demonstrate how, and
                 * to allow us to reopen the last opened document when the app starts.
                 */
                context?.contentResolver?.takePersistableUriPermission(
                    documentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                documentUri?.let {
                    binding.chatView.viewModel.sendFile(File(it, TypeFile.FILE ))
                }


            }
        }
    }
}