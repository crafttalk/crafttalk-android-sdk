package com.crafttalk.chat.ui.chat_view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.crafttalk.chat.R
import com.crafttalk.chat.data.local.pref.Uuid
import com.crafttalk.chat.data.remote.loader_service.BodyStructureUploadFile
import com.crafttalk.chat.data.remote.loader_service.LoaderInterface
import com.crafttalk.chat.data.remote.loader_service.Uploader
import com.crafttalk.chat.ui.chat_view.view_model.ChatViewModel
import com.crafttalk.chat.ui.file_viewer.BottomSheetFileViewer
import com.crafttalk.chat.ui.file_viewer.Option
import com.crafttalk.chat.utils.ConstantsUtils.URL_UPLOAD_HOST
import com.crafttalk.chat.utils.ConstantsUtils.URL_UPLOAD_NAMESPACE
import com.crafttalk.chat.utils.hideSoftKeyboard
import kotlinx.android.synthetic.main.fragment_entry_field.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import android.graphics.Bitmap
import android.widget.SeekBar
import com.google.android.material.snackbar.Snackbar


class EntryFieldFragment: Fragment(), View.OnClickListener, BottomSheetFileViewer.Listener {

    private lateinit var viewModel: ChatViewModel
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val REQUEST_CODE_DOCUMENT = 4
        private const val REQUEST_CODE_IMAGE = 5
        private const val REQUEST_CAMERA_CODE = 10
        private const val REQUEST_CAMERA_PERMISSION_CODE = 11
    }

    override fun onModalOptionSelected(tag: String?, option: Option) {
        when (option.id) {
            R.id.document -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "application/*"
                    putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                }
                if (intent.resolveActivity(context!!.packageManager) != null) {
                    startActivityForResult(intent, REQUEST_CODE_DOCUMENT)
                }
            }
            R.id.image -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "image/*"
                }
                if (intent.resolveActivity(context!!.packageManager) != null) {
                    startActivityForResult(intent, REQUEST_CODE_IMAGE)
                }
            }
            R.id.camera -> {
                when {
                    checkSelfPermission(context!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                        createPhoto()
                    }
                    else -> {
                        requestPermissions(
                            arrayOf(Manifest.permission.CAMERA),
                            REQUEST_CAMERA_PERMISSION_CODE
                        )
                    }
                }
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createPhoto()
            } else {
                Snackbar.make(entry_field, "Разрешите использовать камеру для этого приложения", Snackbar.LENGTH_LONG).show()
            }
        }
    }


    private fun createPhoto(requestCode: Int = REQUEST_CAMERA_CODE) {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, requestCode)
    }


    override fun onClick(view: View) {
        when(view.id) {
            R.id.send_message -> {
                val message = entry_field.text.toString().trim()
                if (message.isNotEmpty()) {
                    hideSoftKeyboard(this.view)
                    viewModel.sendMessage(message)
                }
                else {
                    BottomSheetFileViewer.Builder()
                        .add(R.menu.options)
                        .setListener(this)
                        .show((context as FragmentActivity).supportFragmentManager)
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_entry_field, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAllListeners()
    }

    private fun setAllListeners() {
        send_message.setOnClickListener(this)
        entry_field.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if ((s?:"").isEmpty()) {
                    send_message.setImageResource(R.drawable.ic_attach_file)
                    send_message.rotation = 45f
                }
                else {
                    send_message.setImageResource(R.drawable.ic_send)
                    send_message.rotation = 0f
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


    private fun getNameFileFromUri(uri: Uri): String? {
        val cursor = activity!!.contentResolver.query(uri, null, null, null, null, null)
        var fileName: String? = null
        try {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
        finally {
            cursor!!.close()
        }
        return fileName
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_DOCUMENT -> {
                    data?.data?.let {
                        sentFile(it)
                    }
                }
                REQUEST_CODE_IMAGE -> {
                    data?.data?.let { fullImageUri ->
                        val photoBitmap = MediaStore.Images.Media.getBitmap(context!!.contentResolver, fullImageUri)

                        sentImage(photoBitmap, fullImageUri)

                        /*
                        scope.launch {
                            getNameFileFromUri(fullImageUri)?.let { fileName ->

//                                val req =
//                                    (Uploader
//                                        .setDefaultHostServerName(URL_UPLOAD_HOST)
//                                        .setDefaultNamespaceClientInSocket(URL_UPLOAD_NAMESPACE)
//                                        .service() as LoaderInterface
//                                    ).uploadFile(
//                                        BodyStructureUploadFile(
//                                            fileName = fileName,
//                                            uuid = Uuid.generateUUID(false),
//                                            fileB64 = Uploader.imagePreparer.convertBitmapToBase64(photoBitmap)
//                                        )
//                                    )
//

                                BodyStructureUploadFile(
                                    fileName = fileName,
                                    uuid = Uuid.generateUUID(false),
                                    fileB64 = Uploader.imagePreparer.convertBitmapToBase64(photoBitmap)
                                )

//                                req.enqueue(object:retrofit2.Callback<String> {
//                                    override fun onFailure(call: Call<String>, t: Throwable) {
//                                        Log.d("onActivityResult", "Error - ${t.message}; ${Uuid.generateUUID(false)}")
//                                    }
//                                    override fun onResponse(call: Call<String>, response: Response<String>) {
//                                        Log.d("onActivityResult", "onResponse - ${response.message()} ${response.body()};; ${Uuid.generateUUID(false)}")
//                                    }
//                                })

                            }
                        }

                        */
                    }

                }
                REQUEST_CAMERA_CODE -> {
                    data?.extras?.let {bundle ->
                        val resPhotoBitmap = bundle.get("data") as Bitmap
                        sentImage(resPhotoBitmap)
                    }
                }
            }
        }
    }

    private fun sentImage(bitmap: Bitmap, imageUri: Uri? = null) {
        Log.d("PHOTO_create", "sentImage")
        scope.launch {
            val fileName = if (imageUri == null) {
                "createPhoto${System.currentTimeMillis()}.jpg"
            }
            else {
                getNameFileFromUri(imageUri)
            }

            Log.d("PHOTO_create", "name = ${fileName}")

            fileName?.let { name ->
                Log.d("PHOTO_create", "upload image")
                val req = (Uploader
                    .setDefaultHostServerName(URL_UPLOAD_HOST)
                    .setDefaultNamespaceClientInSocket(URL_UPLOAD_NAMESPACE)
                    .service() as LoaderInterface
                        ).uploadFile(
                    BodyStructureUploadFile(
                        fileName = name,
                        uuid = Uuid.generateUUID(false),
                        fileB64 = Uploader.imagePreparer.convertBitmapToBase64(bitmap)
                    )
                )

//                req.enqueue(object:retrofit2.Callback<String> {
//                    override fun onFailure(call: Call<String>, t: Throwable) {
//                        Log.d("PHOTO_create", "Error - ${t.message}; ${Uuid.generateUUID(false)}")
//                    }
//                    override fun onResponse(call: Call<String>, response: Response<String>) {
//                        Log.d("PHOTO_create", "onResponse - ${response.message()} ${response.body()};; ${Uuid.generateUUID(false)}")
//                    }
//                })

            }

        }
    }

    private fun sentFile(fullFileUri: Uri) {
        val fileInputStream = context!!.contentResolver.openInputStream(fullFileUri)!!

        scope.launch {
            getNameFileFromUri(fullFileUri)?.let { fileName ->
                val req =
                    (Uploader
                        .setDefaultHostServerName(URL_UPLOAD_HOST)
                        .setDefaultNamespaceClientInSocket(URL_UPLOAD_NAMESPACE)
                        .service() as LoaderInterface
                            ).uploadFile(
                        BodyStructureUploadFile(
                            fileName = fileName,
                            uuid = Uuid.generateUUID(false),
                            fileB64 = Uploader.filePreparer.convertFileToBase64(fileInputStream)
                        )
                    )

                req.enqueue(object:retrofit2.Callback<String> {
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.d("onActivityResult", "Error - ${t.message} ${Uuid.generateUUID(false)}")
                    }
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        Log.d("onActivityResult", "onResponse - ${response.message()} ${response.body()}; ${Uuid.generateUUID(false)}")
                    }
                })
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Fragment", "onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Fragment", "onDestroy")
    }

    fun setViewModel(viewModel: ChatViewModel) {
        this.viewModel = viewModel
    }

}