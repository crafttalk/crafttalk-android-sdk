package com.crafttalk.sampleChat.web_view

import com.crafttalk.sampleChat.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.net.Uri
import android.widget.Toast
import android.content.ActivityNotFoundException
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.crafttalk.sampleChat.web_view.contracts.PickFileContract
import com.crafttalk.sampleChat.web_view.contracts.PickFileModel
import com.crafttalk.sampleChat.web_view.contracts.TakePicture
import com.crafttalk.sampleChat.web_view.file_viewer.BottomSheetFileViewer
import com.crafttalk.sampleChat.web_view.file_viewer.Option
import com.crafttalk.sampleChat.web_view.utils.pickFiles
import com.crafttalk.sampleChat.web_view.utils.pickImageFromCamera
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.fragment_chat.*
import java.util.*
import android.webkit.*
import android.webkit.WebView.setWebContentsDebuggingEnabled
import com.crafttalk.sampleChat.web_view.data.api.NotificationApi
import com.crafttalk.sampleChat.web_view.data.repository.NotificationRepository
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.concurrent.thread

class WebViewActivity: AppCompatActivity(R.layout.activity_web_view), BottomSheetFileViewer.Listener {

    private var uploadMessage: ValueCallback<Uri?>? = null
    private var uploadMessages: ValueCallback<Array<Uri?>?>? = null
    private var callbackResult: (isGranted: Boolean) -> Unit = {}
    private var visitorUuid: String? = null
    private var notificationRepository: NotificationRepository? = null

    private val requestPermission: ActivityResultLauncher<String> = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
        callbackResult(isGranted)
    }
    private val takePictureLauncher = registerForActivityResult(TakePicture()) { uri ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (uploadMessages == null) return@registerForActivityResult
            uploadMessages?.onReceiveValue(arrayOf(uri))
            uploadMessages = null
        } else {
            if (null == uploadMessage) return@registerForActivityResult
            uploadMessage?.onReceiveValue(uri)
            uploadMessage = null
        }
    }
    private val takeFileLauncher = registerForActivityResult(PickFileContract()) { listUri ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (uploadMessages == null) return@registerForActivityResult
            uploadMessages?.onReceiveValue(listUri)
            uploadMessages = null
        } else {
            if (null == uploadMessage) return@registerForActivityResult
            uploadMessage?.onReceiveValue(listUri.firstOrNull())
            uploadMessage = null
        }
    }

    /*
    private var keyboardListenersAttached = false
    private var rootLayout: ViewGroup? = null
    private var fullHeightWebView: Int? = null

    private val keyboardLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        if (fullHeightWebView == null) {
            fullHeightWebView = rootLayout?.height
            return@OnGlobalLayoutListener
        }
        val fullHeightWebView = fullHeightWebView ?: return@OnGlobalLayoutListener
        val currentHeightWebView = rootLayout?.height ?: return@OnGlobalLayoutListener
        if (fullHeightWebView <= currentHeightWebView) {
            onHideKeyboard()
        } else {
            onShowKeyboard()
        }
    }

    private fun onShowKeyboard() {
        // todo
    }

    private fun onHideKeyboard() {
        // todo
    }

    private fun attachKeyboardListeners() {
        if (keyboardListenersAttached) {
            return
        }
        rootLayout = root_layout
        rootLayout?.viewTreeObserver?.addOnGlobalLayoutListener(keyboardLayoutListener)
        keyboardListenersAttached = true
    }
    */

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        notificationRepository = NotificationRepository(
            Retrofit.Builder()
                .baseUrl("${getString(R.string.webUrlChatScheme)}://${getString(R.string.webUrlChatHost)}")
                .addConverterFactory(GsonConverterFactory.create(Gson()))
                .build()
                .create(NotificationApi::class.java)
        )

//        attachKeyboardListeners()

        web_view.apply {
            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
            settings.domStorageEnabled = true
            settings.defaultTextEncodingName = "utf-8"

            loadUrl("${getString(R.string.webUrlChatScheme)}://${getString(R.string.webUrlChatHost)}/webchat/${getString(R.string.webUrlChatNameSpace)}")

            setWebContentsDebuggingEnabled(true)
            addJavascriptInterface(JavaScriptInterface(this@WebViewActivity), "Android")
            setDownloadListener { url, _, _, mimeType, _ ->
                loadUrl(JavaScriptInterface.getBase64StringFromBlobUrl(url, mimeType))
            }

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    Log.d("CTALK_TEST_DOWNLOAD", "consoleMessage: ${consoleMessage?.message()};")
                    getVisitorUuid()
                    return true
                }

                // For Android 3.0+
                fun openFileChooser(uploadMsg: ValueCallback<Uri?>?) {
                    uploadMessage = uploadMsg
                    openFileChooser()
                }

                // For Android 3.0+
                fun openFileChooser(uploadMsg: ValueCallback<*>?, acceptType: String?) {
                    uploadMessage = uploadMsg as? ValueCallback<Uri?>?
                    openFileChooser()
                }

                //For Android 4.1
                fun openFileChooser(uploadMsg: ValueCallback<Uri?>?, acceptType: String?, capture: String?) {
                    uploadMessage = uploadMsg
                    openFileChooser()
                }

                // For Lollipop 5.0+ Devices
                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun onShowFileChooser(mWebView: WebView?, filePathCallback: ValueCallback<Array<Uri?>?>, fileChooserParams: FileChooserParams?): Boolean {
                    if (uploadMessages != null) {
                        uploadMessages?.onReceiveValue(null)
                        uploadMessages = null
                    }
                    uploadMessages = filePathCallback
                    try {
                        openFileChooser()
                    } catch (e: ActivityNotFoundException) {
                        uploadMessages = null
                        Toast.makeText(
                            this@WebViewActivity,
                            "Cannot Open File Chooser",
                            Toast.LENGTH_LONG
                        ).show()
                        return false
                    }
                    return true
                }
            }
        }
    }

    private fun WebView.getVisitorUuid() {
        if (visitorUuid == null) {
            val key = "webchat-${getString(R.string.webUrlChatNameSpace)}-uuid"
            evaluateJavascript("javascript:window.localStorage.getItem('$key')") {
                Log.d("CTALK_TEST_DATA", "get - $it;")
                visitorUuid = it
                if (visitorUuid != null) {
                    thread {
                        notificationRepository?.subscribe(visitorUuid, getString(R.string.webUrlChatNameSpace))
                    }
                    Handler().postDelayed({
                        notificationRepository?.checkSubscription(visitorUuid!!, getString(R.string.webUrlChatNameSpace), false) {
                            Log.d("CTALK_TEST_DATA", "checkSubscription - $it;")
                        }
                    }, 10000)
                }
            }
        }
    }

    private fun openFileChooser() {
        BottomSheetFileViewer.Builder()
            .add(R.menu.file_chooser)
            .setListener(this)
            .show(supportFragmentManager)
    }

    private fun createPictureFromCamera() {
        pickImageFromCamera(
            takePicture = takePictureLauncher,
            noPermission = { permission: String, actionsAfterObtainingPermission: () -> Unit ->
                requestPermission(
                    permission,
                    getString(com.crafttalk.chat.R.string.com_crafttalk_chat_requested_permission_camera),
                    actionsAfterObtainingPermission
                )
            },
            context = this
        )
    }

    private fun requestPermission(permission: String, message: String, action: () -> Unit) {
        callbackResult = { isGranted ->
            if (isGranted) {
                action()
            } else {
                Snackbar.make(web_view, message, Snackbar.LENGTH_LONG).show()
            }
        }
        requestPermission.launch(permission)
    }

    private fun createFileFromGallery(typeFile: String, isMultiple: Boolean = false) {
        pickFiles(
            pickFile = takeFileLauncher,
            pickFileModel = PickFileModel(
                typeFile = typeFile,
                isMultiple = isMultiple
            ),
            noPermission = { permission: String, actionsAfterObtainingPermission: () -> Unit ->
                requestPermission(
                    permission,
                    getString(com.crafttalk.chat.R.string.com_crafttalk_chat_requested_permission_storage),
                    actionsAfterObtainingPermission
                )
            },
            context = this
        )
    }

    override fun onModalOptionSelected(tag: String?, option: Option) {
        when (option.id) {
            R.id.document -> {
                createFileFromGallery(
                    typeFile = "application/*"
                )
            }
            R.id.image -> {
                createFileFromGallery(
                    typeFile = "image/*"
                )
            }
            R.id.camera -> createPictureFromCamera()
        }
    }

    override fun onCloseBottomSheet() {}

    /*
    override fun onDestroy() {
        super.onDestroy()

        if (keyboardListenersAttached) {
            rootLayout?.viewTreeObserver?.removeGlobalOnLayoutListener(keyboardLayoutListener)
        }
    }
    */
}