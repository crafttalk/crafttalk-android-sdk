package com.crafttalk.sampleChat.web_view

import com.crafttalk.sampleChat.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.widget.Toast
import android.content.ActivityNotFoundException
import android.os.Build
import android.webkit.WebChromeClient.FileChooserParams
import android.webkit.WebView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_web_view.*
import android.webkit.WebView.setWebContentsDebuggingEnabled

class WebViewActivity: AppCompatActivity(R.layout.activity_web_view) {

    private var uploadMessage: ValueCallback<Uri?>? = null
    private var uploadMessages: ValueCallback<Array<Uri?>?>? = null

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

//        attachKeyboardListeners()

        web_view.apply {
            settings.javaScriptEnabled = true
            settings.allowFileAccess = true
            loadUrl("${getString(R.string.webUrlChatScheme)}://${getString(R.string.webUrlChatHost)}/webchat/${getString(R.string.webUrlChatNameSpace)}")

            setWebContentsDebuggingEnabled(true);

            webChromeClient = object : WebChromeClient() {

                // For Android 3.0+
                fun openFileChooser(uploadMsg: ValueCallback<Uri?>?) {
                    uploadMessage = uploadMsg
                    val i = Intent(Intent.ACTION_GET_CONTENT)
                    i.addCategory(Intent.CATEGORY_OPENABLE)
                    i.type = "*/*"
                    openFileChooser(Intent.createChooser(i, "File Chooser"))
                }

                // For Android 3.0+
                fun openFileChooser(uploadMsg: ValueCallback<*>?, acceptType: String?) {
                    uploadMessage = uploadMsg as? ValueCallback<Uri?>?
                    val i = Intent(Intent.ACTION_GET_CONTENT)
                    i.addCategory(Intent.CATEGORY_OPENABLE)
                    i.type = "*/*"
                    openFileChooser(Intent.createChooser(i, "File Browser"))
                }

                //For Android 4.1
                fun openFileChooser(uploadMsg: ValueCallback<Uri?>?, acceptType: String?, capture: String?) {
                    uploadMessage = uploadMsg
                    val i = Intent(Intent.ACTION_GET_CONTENT)
                    i.addCategory(Intent.CATEGORY_OPENABLE)
                    i.type = "*/*"
                    openFileChooser(Intent.createChooser(i, "File Chooser"))
                }

                // For Lollipop 5.0+ Devices
                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun onShowFileChooser(mWebView: WebView?, filePathCallback: ValueCallback<Array<Uri?>?>, fileChooserParams: FileChooserParams?): Boolean {
                    if (uploadMessages != null) {
                        uploadMessages?.onReceiveValue(null)
                        uploadMessages = null
                    }
                    uploadMessages = filePathCallback
                    val intent = fileChooserParams?.createIntent()
                    try {
                        openFileChooser(intent!!)
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

    private fun openFileChooser(intent: Intent) {
        resultLauncher.launch(intent)
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (uploadMessages == null) return@registerForActivityResult
            uploadMessages?.onReceiveValue(FileChooserParams.parseResult(result.resultCode, result.data))
            uploadMessages = null
        } else {
            if (null == uploadMessage) return@registerForActivityResult
            val resultData = if (result.data == null || result.resultCode != RESULT_OK) {
                null
            } else {
                result.data?.data
            }
            uploadMessage?.onReceiveValue(resultData)
            uploadMessage = null
        }
    }

    /*
    override fun onDestroy() {
        super.onDestroy()

        if (keyboardListenersAttached) {
            rootLayout?.viewTreeObserver?.removeGlobalOnLayoutListener(keyboardLayoutListener)
        }
    }
    */
}