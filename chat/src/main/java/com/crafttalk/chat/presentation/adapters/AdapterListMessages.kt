package com.crafttalk.chat.presentation.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.message.MessageType.*
import com.crafttalk.chat.presentation.holders.*
import com.crafttalk.chat.presentation.model.*
import com.crafttalk.chat.utils.ChatAttr
import java.text.SimpleDateFormat

class AdapterListMessages(
    private val inflater: LayoutInflater,
    private var mData: List<MessageModel>,
    private val actionListener: ActionListener,
    private val updateSizeMessageListener: UpdateSizeMessageListener,
    private val openFile: (context: Context, fileUrl: String) -> Unit,
    private val openImage: (activity: Activity, imageUrl: String, width: Int, height: Int) -> Unit,
    private val openGif: (activity: Activity, gifUrl: String, width: Int, height: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val scaleRatio = inflater.context.resources.displayMetrics.density
    @SuppressLint("SimpleDateFormat")
    val formatTime = SimpleDateFormat("dd.MM.yyyy HH:mm")

    fun setData(newData: List<MessageModel>) {
        mData = newData
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun getItemViewType(position: Int): Int {
        return mData[position].typeMessage.valueType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MessageType.USER_TEXT_MESSAGE.valueType -> {
                HolderUserTextMessage(
                    inflater.inflate(R.layout.item_user_text_message, parent, false)
                )
            }
            MessageType.OPERATOR_TEXT_MESSAGE.valueType -> {
                HolderOperatorTextMessage(
                    inflater.inflate(R.layout.item_server_text_message, parent, false)
                )
            }
            MessageType.USER_IMAGE_MESSAGE.valueType -> {
                HolderUserImageMessage(
                    inflater.inflate(R.layout.item_user_image_message, parent, false)
                ) { imageUrl, width, height -> openImage(inflater.context as Activity, imageUrl, width, height) }
            }
            MessageType.OPERATOR_IMAGE_MESSAGE.valueType -> {
                HolderOperatorImageMessage(
                    inflater.inflate(R.layout.item_server_image_message, parent, false)
                ) { imageUrl, width, height -> openImage(inflater.context as Activity, imageUrl, width, height) }
            }
            MessageType.USER_FILE_MESSAGE.valueType -> {
                HolderUserFileMessage(
                    inflater.inflate(R.layout.item_user_file_message, parent, false)
                ) { fileUrl -> openFile(inflater.context, fileUrl) }
            }
            MessageType.OPERATOR_FILE_MESSAGE.valueType -> {
                HolderOperatorFileMessage(
                    inflater.inflate(R.layout.item_server_file_message, parent, false)
                ) { fileUrl -> openFile(inflater.context, fileUrl) }
            }
            MessageType.USER_GIF_MESSAGE.valueType -> HolderUserGifMessage(inflater.inflate(R.layout.item_user_image_message, parent, false))
            { gifUrl, width, height -> openGif(inflater.context as Activity, gifUrl, width, height) }
            MessageType.OPERATOR_GIF_MESSAGE.valueType -> HolderOperatorGifMessage(inflater.inflate(R.layout.item_server_image_message, parent, false))
            { gifUrl, width, height -> openGif(inflater.context as Activity, gifUrl, width, height) }
            MessageType.DEFAULT_MESSAGE.valueType -> {
                HolderDefaultMessage(inflater.inflate(R.layout.item_default_message, parent, false))
            }
            else -> throw Exception("Fail in AdapterListMessages with MessageViewType")
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is HolderUserTextMessage -> {
                val textMessage = mData[position] as TextMessage
                setTimeMessageWithCheck(viewHolder.time, textMessage)
                // set content
                viewHolder.message.text = textMessage.message
                // set color
                viewHolder.message.setTextColor(ChatAttr.mapAttr["color_text_user_message"] as Int)
                // set dimension
                viewHolder.message.textSize = (ChatAttr.mapAttr["size_user_message"] as Float)/scaleRatio
                // set bg
                ViewCompat.setBackgroundTintList(viewHolder.message, ColorStateList.valueOf(ChatAttr.mapAttr["color_bg_user_message"] as Int))
            }
            is HolderOperatorTextMessage -> {
                val textMessage = mData[position] as TextMessage
                setTimeMessageDefault(viewHolder.time, textMessage)
                // set content
                viewHolder.message.movementMethod = LinkMovementMethod.getInstance()
                viewHolder.message.text = textMessage.message
                // set color
                viewHolder.message.setTextColor(ChatAttr.mapAttr["color_text_server_message"] as Int)

                // set bg
                viewHolder.message.setBackgroundColor(0)
                viewHolder.message.setBackgroundResource(R.drawable.background_item_simple_server_message)

                textMessage.actions?.let {
                    viewHolder.listActions.adapter = AdapterAction(inflater, it, actionListener)
                }
                // set bg color
                ViewCompat.setBackgroundTintList(viewHolder.message, ColorStateList.valueOf(ChatAttr.mapAttr["color_bg_server_message"] as Int))
            }
            is HolderUserImageMessage -> {
                val imageMessage = mData[position] as ImageMessage
                viewHolder.imageUrl = imageMessage.imageUrl

                loadAndSetImage(viewHolder.img, imageMessage)
                setTimeMessageWithCheck(viewHolder.time, imageMessage)
            }
            is HolderOperatorImageMessage -> {
                val imageMessage = mData[position] as ImageMessage
                viewHolder.imageUrl = imageMessage.imageUrl

                loadAndSetImage(viewHolder.img, imageMessage)
                setTimeMessageDefault(viewHolder.time, imageMessage)
            }
            is HolderOperatorFileMessage -> {
                val fileMessage = mData[position] as FileMessage
                viewHolder.fileUrl = fileMessage.fileUrl

                setFile(viewHolder.fileIcon)
                setTimeMessageDefault(viewHolder.time, fileMessage)
            }
            is HolderUserFileMessage -> {
                val fileMessage = mData[position] as FileMessage
                viewHolder.fileUrl = fileMessage.fileUrl

                setFile(viewHolder.fileIcon)
                setTimeMessageWithCheck(viewHolder.time, fileMessage)
            }
            is HolderUserGifMessage -> {
                val gifMessage = mData[position] as GifMessage
                viewHolder.gifUrl = gifMessage.gifUrl

                loadAndSetGif(viewHolder.gif, gifMessage)
                setTimeMessageWithCheck(viewHolder.time, gifMessage)
            }
            is HolderOperatorGifMessage -> {
                val gifMessage = mData[position] as GifMessage
                viewHolder.gifUrl = gifMessage.gifUrl

                loadAndSetGif(viewHolder.gif, gifMessage)
                setTimeMessageDefault(viewHolder.time, gifMessage)
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
    private fun setTimeMessageWithCheck(timeView: TextView, message: MessageModel) {
        setTimeMessageDefault(timeView, message)

        when (message.stateCheck) {
            VISITOR_MESSAGE -> {}
            RECEIVED_BY_MEDIATO -> {
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
            RECEIVED_BY_OPERATOR -> {
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
    private fun setTimeMessageDefault(timeView: TextView, message: MessageModel) {
        // set content
        timeView.text = "${message.authorName} ${formatTime.format(message.timestamp)}"
        timeView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

        // set color
        timeView.setTextColor(ChatAttr.mapAttr["color_time_mark"] as Int)
        // set dimension
        timeView.textSize = (ChatAttr.mapAttr["size_time_mark"] as Float)/scaleRatio
    }

    @SuppressLint("ResourceAsColor")
    private fun loadAndSetImage(imageView: ImageView, imageMessage: ImageMessage) {
        val (widthInPx, heightInPx) = getSizeScreenInPx()
        if (imageMessage.height == 0 && imageMessage.width == 0) {
            Glide.with(inflater.context)
                .asBitmap()
                .load(imageMessage.imageUrl)
                .error(R.color.default_color_company)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onLoadCleared(placeholder: Drawable?) {
                        imageView.setImageDrawable(placeholder)
                    }
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val (newWidthInPx, newHeightInPx) =
                            if (resource.height > resource.width) {
                                Pair((heightInPx * 0.4 * resource.width / resource.height).toInt(), (heightInPx * 0.4).toInt())
                            } else {
                                Pair((widthInPx * 0.7).toInt(), (widthInPx * 0.7 * resource.height / resource.width).toInt())
                            }

                        imageView.setImageBitmap(
                            Bitmap.createScaledBitmap(
                                resource,
                                newWidthInPx,
                                newHeightInPx,
                                false
                            )
                        )

                        updateSizeMessageListener.updateData(imageMessage.idKey, resource.height, resource.width)

                    }
                })
        } else {
            Glide.with(inflater.context)
                .load(imageMessage.imageUrl)
                .apply(RequestOptions().override(
                    if (imageMessage.height > imageMessage.width) (heightInPx * 0.4 * imageMessage.width / imageMessage.height).toInt() else (widthInPx * 0.7).toInt(),
                    if (imageMessage.height > imageMessage.width) (heightInPx * 0.4).toInt() else (widthInPx * 0.7 * imageMessage.height / imageMessage.width).toInt()
                ))
                .error(R.color.default_color_company)
                .into(imageView)
        }
    }

    private fun loadAndSetGif(imageView: ImageView, gifMessage: GifMessage) {
        val (widthInPx, heightInPx) = getSizeScreenInPx()
        if (gifMessage.height == 0 && gifMessage.width == 0) {
            Glide.with(inflater.context)
                .asBitmap()
                .load(gifMessage.gifUrl)
                .error(R.color.default_color_company)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onLoadCleared(placeholder: Drawable?) {
                        imageView.setImageDrawable(placeholder)
                    }
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val (newWidthInPx, newHeightInPx) =
                            if (resource.height > resource.width) {
                                Pair((heightInPx * 0.4 * resource.width / resource.height).toInt(), (heightInPx * 0.4).toInt())
                            } else {
                                Pair((widthInPx * 0.7).toInt(), (widthInPx * 0.7 * resource.height / resource.width).toInt())
                            }

                        Glide.with(inflater.context)
                            .asGif()
                            .load(gifMessage.gifUrl)
                            .apply(RequestOptions().override(newWidthInPx, newHeightInPx))
                            .error(R.color.default_color_company)
                            .into(imageView)

                        updateSizeMessageListener.updateData(gifMessage.idKey, resource.height, resource.width)
                    }
                })
        } else {
            Glide.with(inflater.context)
                .asGif()
                .load(gifMessage.gifUrl)
                .apply(RequestOptions().override(
                    if (gifMessage.height > gifMessage.width) (heightInPx * 0.4 * gifMessage.width / gifMessage.height).toInt() else (widthInPx * 0.7).toInt(),
                    if (gifMessage.height > gifMessage.width) (heightInPx * 0.4).toInt() else (widthInPx * 0.7 * gifMessage.height / gifMessage.width).toInt()
                ))
                .error(R.color.default_color_company)
                .into(imageView)
        }
    }

    private fun setFile(fileView: ImageView) {
        val (widthInPx, heightInPx) = getSizeScreenInPx()

        fileView.layoutParams.let {
            it.height = (widthInPx * 0.1).toInt()
            it.width = (widthInPx * 0.1).toInt()
        }
    }

    interface UpdateSizeMessageListener {
        fun updateData(idKey: Long, height: Int, width: Int)
    }

    interface ActionListener {
        fun actionSelect(actionId: String)
    }

}