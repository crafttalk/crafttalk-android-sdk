package com.crafttalk.chat.ui.chat_view.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.data.local.db.entity.Message
import com.crafttalk.chat.data.local.db.entity.MessageViewType
import com.crafttalk.chat.data.local.db.entity.MessageViewType.Companion.getMessageViewType
import com.crafttalk.chat.data.model.MessageType
import com.crafttalk.chat.ui.chat_view.ShowImageDialog
import com.crafttalk.chat.ui.chat_view.holders.*
import com.crafttalk.chat.utils.ChatAttr
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat


class AdapterListMessages(private val inflater: LayoutInflater, private var mData: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val scaleRatio = inflater.context.resources.displayMetrics.density
    private val scope = CoroutineScope(Dispatchers.IO)
    @SuppressLint("SimpleDateFormat")
    val formatTime = SimpleDateFormat("dd.MM.yyyy HH:mm")

    fun setData(newData: List<Message>) {
        mData = newData
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun getItemViewType(position: Int): Int {
        return getMessageViewType(mData[position]).valueType
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("AdapterListMessages", "onCreateViewHolder - ${viewType}")
        return when (viewType) {
            MessageViewType.USER_TEXT_MESSAGE.valueType -> {
                val view = inflater.inflate(R.layout.item_user_text_message, parent, false)
                HolderUserTextMessage(view)
            }
            MessageViewType.USER_IMAGE_MESSAGE.valueType -> {
                Log.d("AdapterListMessages", "USER_IMAGE_MESSAGE")
                val view = inflater.inflate(R.layout.item_user_image_message, parent, false)
                HolderUserImageMessage(view) { imageUrl ->
                    ShowImageDialog.Builder(inflater.context as Activity)
                        .setUrl(imageUrl)
                        .show()
                }
            }
            MessageViewType.USER_FILE_MESSAGE.valueType -> {
                Log.d("AdapterListMessages", "USER_FILE_MESSAGE")
                val view = inflater.inflate(R.layout.item_user_file_message, parent, false)
                HolderUserFileMessage(view) { fileUrl ->
                    val intentView = Intent(Intent.ACTION_VIEW).apply {
                        data = fileUrl.toUri()
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    val intentChooser = Intent.createChooser(intentView, inflater.context.getString(R.string.string_chooser_open_file_action_view))
                    if (intentView.resolveActivity(inflater.context.packageManager) != null) {
                        inflater.context.startActivity(intentChooser)
                    }
                }
            }
            MessageViewType.SERVER_TEXT_MESSAGE.valueType -> {
                val view = inflater.inflate(R.layout.item_server_text_message, parent, false)
                HolderServerTextMessage(view)
            }
            MessageViewType.SERVER_IMAGE_MESSAGE.valueType -> {
                Log.d("AdapterListMessages", "HolderServerImageMessage")
                val view = inflater.inflate(R.layout.item_server_image_message, parent, false)
                HolderServerImageMessage(view) { imageUrl ->
                    ShowImageDialog.Builder(inflater.context as Activity)
                        .setUrl(imageUrl)
                        .show()
                }
            }
            MessageViewType.SERVER_FILE_MESSAGE.valueType -> {
                Log.d("AdapterListMessages", "SERVER_FILE_MESSAGE")
                val view = inflater.inflate(R.layout.item_server_file_message, parent, false)
                HolderServerFileMessage(view) { fileUrl ->
                    val intentView = Intent(Intent.ACTION_VIEW).apply {
                        data = fileUrl.toUri()
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    val intentChooser = Intent.createChooser(intentView, inflater.context.getString(R.string.string_chooser_open_file_action_view))
                    if (intentView.resolveActivity(inflater.context.packageManager) != null) {
                        inflater.context.startActivity(intentChooser)
                    }
                }
            }
            MessageViewType.DEFAULT_MESSAGE.valueType -> {
                val view = inflater.inflate(R.layout.item_default_message, parent, false)
                HolderDefaultMessage(view)
            }
            else -> throw Exception("Fail in AdapterListMessages with MessageViewType")
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val messageObg = mData[position]

        when (viewHolder.itemViewType) {
            MessageViewType.USER_TEXT_MESSAGE.valueType -> {
                val holder = viewHolder as HolderUserTextMessage

                setTimeMessageWithCheck(holder.time, messageObg)
                // set content
                holder.message.text = messageObg.message
                // set color
                holder.message.setTextColor(ChatAttr.mapAttr["color_text_user_message"] as Int)
                // set dimension
                holder.message.textSize = (ChatAttr.mapAttr["size_user_message"] as Float)/scaleRatio
                // set bg
                ViewCompat.setBackgroundTintList(holder.message, ColorStateList.valueOf(ChatAttr.mapAttr["color_bg_user_message"] as Int))
            }
            MessageViewType.USER_IMAGE_MESSAGE.valueType -> {
                val holder = viewHolder as HolderUserImageMessage
                holder.imageUrl = messageObg.attachmentUrl!!

                loadAndSetImage(holder.img, messageObg.attachmentUrl)
                setTimeMessageWithCheck(holder.time, messageObg)
            }
            MessageViewType.USER_FILE_MESSAGE.valueType -> {
                val holder = viewHolder as HolderUserFileMessage
                holder.fileUrl = messageObg.attachmentUrl!!

                setFile(holder.fileIcon)
                setTimeMessageWithCheck(holder.time, messageObg)
            }
            MessageViewType.SERVER_TEXT_MESSAGE.valueType -> {
                val holder = viewHolder as HolderServerTextMessage

                setTimeMessageDefault(holder.time, messageObg)
                // set content + set color
                val data = """
                    <html>
                    <head>
                    <style type="text/css">
                    @font-face {
                        font-family: 'Ubuntu';
                        src:url("file:///android_res/font/ubuntu_light.ttf")
                    }
                    body {
                        font-family: 'Ubuntu';
                        font-weight: 300;
                        color:${ChatAttr.mapAttr["color_text_server_message"]}
                    }
                    </style>
                    </head>
                    <body>
                        ${messageObg.message?.replace("\n", "<br>")}
                    </body>
                    </html>
                    """

                holder.message.loadDataWithBaseURL(null, data, "text/html", "utf-8", null)
                // set bg resource
                holder.message.setBackgroundColor(0)
                holder.message.setBackgroundResource(R.drawable.background_item_simple_server_message)

                messageObg.actions?.let {
                    holder.listActions.adapter = AdapterAction(inflater, it)
                }
                // set dimension
                holder.message.settings.apply {
                    this.defaultFontSize = ((ChatAttr.mapAttr["size_server_message"] as Float)/scaleRatio).toInt()
                }
                // set bg
                ViewCompat.setBackgroundTintList(holder.message, ColorStateList.valueOf(ChatAttr.mapAttr["color_bg_server_message"] as Int))
            }
            MessageViewType.SERVER_IMAGE_MESSAGE.valueType -> {
                val holder = viewHolder as HolderServerImageMessage
                holder.imageUrl = messageObg.attachmentUrl!!

                loadAndSetImage(holder.img, messageObg.attachmentUrl)
                setTimeMessageDefault(holder.time, messageObg)
            }
            MessageViewType.SERVER_FILE_MESSAGE.valueType -> {
                val holder = viewHolder as HolderServerFileMessage
                holder.fileUrl = messageObg.attachmentUrl!!

                setFile(holder.fileIcon)
                setTimeMessageDefault(holder.time, messageObg)
            }
        }
    }

    private fun transformSizeDrawable(idIcon: Int, newSize: Int): Drawable {
        // dont work in Xiaomi
//        val dr = ResourcesCompat.getDrawable(inflater.context.resources, idIcon, null)
//        val bitmap = (dr as BitmapDrawable).bitmap

        val bitmap = BitmapFactory.decodeResource(inflater.context.resources, idIcon)
        return BitmapDrawable(inflater.context.resources, Bitmap.createScaledBitmap(bitmap, newSize, newSize, true))
    }

    private fun getSizeScreenInPx(): Pair<Int, Int> {
        val display = (inflater.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()
        display.getSize(size)
        return Pair(size.x, size.y)
    }

    private fun TextView.setDrawableColor(color: Int) {
        compoundDrawables.filterNotNull().forEach {
            it.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setTimeMessageWithCheck(timeView: TextView, message: Message) {
        setTimeMessageDefault(timeView, message)

        when (message.messageType) {
            MessageType.VISITOR_MESSAGE.valueType -> {}
            MessageType.RECEIVED_BY_MEDIATO.valueType -> {
                timeView.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    transformSizeDrawable(
                        R.drawable.ic_check,
                        (15 * (inflater.context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
                    ),
                    null
                )
            }
            MessageType.RECEIVED_BY_OPERATOR.valueType -> {
                timeView.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    transformSizeDrawable(
                        R.drawable.ic_db_check,
                        (15 * (inflater.context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
                    ),
                    null
                )
            }
        }

        timeView.setDrawableColor(ChatAttr.mapAttr["color_time_mark"] as Int)
    }

    @SuppressLint("SetTextI18n")
    private fun setTimeMessageDefault(timeView: TextView, message: Message) {
        // set content
        timeView.text = "${message.operatorName} ${formatTime.format(message.timestamp)}"
        timeView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

        // set color
        timeView.setTextColor(ChatAttr.mapAttr["color_time_mark"] as Int)
        // set dimension
        timeView.textSize = (ChatAttr.mapAttr["size_time_mark"] as Float)/scaleRatio
    }

    @SuppressLint("ResourceAsColor")
    private fun loadAndSetImage(imageView: ImageView, url: String) {

        scope.launch {
            try {
                val (widthInPx, heightInPx) = getSizeScreenInPx()

                val bitmapImage = Picasso.with(inflater.context).load(url).get()

                imageView.post {
                    bitmapImage?.let {bitmap ->
                        imageView.background = null
                        if (bitmap.height > bitmap.width) {
                            imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, (heightInPx * 0.4 * bitmap.width / bitmap.height).toInt(), (heightInPx * 0.4).toInt(), false))
                        }
                        else {
                            imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, (widthInPx * 0.7).toInt(), (widthInPx * 0.7 * bitmap.height / bitmap.width).toInt(), false))
                        }
                    }
                }
                Log.d("AdapterListMessage", "bitmapImage - $bitmapImage")
            }
            catch (allEx: Exception) {
                imageView.post {
                    imageView.minimumHeight = 400
                    imageView.minimumWidth = 400
                    imageView.setBackgroundColor(R.color.default_color_company)
                }
                Log.d("Ex", "fail; ${allEx.message}; ${allEx.stackTrace}")
            }
        }
    }

    private fun setFile(fileView: ImageView) {
        val (widthInPx, heightInPx) = getSizeScreenInPx()

        fileView.layoutParams.let {
            it.height = (widthInPx * 0.1).toInt()
            it.width = (widthInPx * 0.1).toInt()
        }
    }

}
