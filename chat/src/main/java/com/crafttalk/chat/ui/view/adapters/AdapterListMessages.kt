package com.crafttalk.chat.ui.view.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.data.local.db.entity.Message
import com.crafttalk.chat.data.model.MessageType
import com.crafttalk.chat.ui.view.holders.HolderSimpleServerMessageView
import com.crafttalk.chat.ui.view.holders.HolderSimpleUserMessageView
import java.text.SimpleDateFormat
import androidx.core.graphics.drawable.DrawableCompat





class AdapterListMessages(private val inflater: LayoutInflater, private var mData: List<Message>, private val mapAttr: Map<String, Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val scaleRatio = inflater.context.resources.displayMetrics.density

    @SuppressLint("SimpleDateFormat")
    val formatTime = SimpleDateFormat("dd.MM.yyyy HH:mm")

    fun setData(newData: List<Message>) {
        mData = newData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            -1 -> {
                val view = inflater.inflate(R.layout.item_user_message, parent, false)
                HolderSimpleUserMessageView(view)
            }
            0 -> {
                val view = inflater.inflate(R.layout.item_server_message, parent, false)
                HolderSimpleServerMessageView(view)
            }
            else -> null!!
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun getItemViewType(position: Int): Int {
        return mData[position].isReply.compareTo(true)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val messageObg = mData[position]
        when (viewHolder.itemViewType) {
            -1 -> {
                val holder = viewHolder as HolderSimpleUserMessageView
                // set content
                holder.message.text = messageObg.message
                holder.time.text = "${messageObg.operatorName} ${formatTime.format(messageObg.timestamp)}"
                holder.time.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                when (messageObg.messageType) {
                    MessageType.VISITOR_MESSAGE.valueType -> {}
                    MessageType.RECEIVED_BY_MEDIATO.valueType -> {
                        Log.d("ADAPTER_LIST_MESSAGE", "user message holder: type - RECEIVED_BY_MEDIATO, message - ${mData[position].message}")
                        holder.time.setCompoundDrawablesWithIntrinsicBounds(null, null, transformSizeDrawable(R.drawable.ic_check,
                            (15 * (inflater.context.getResources().displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()), null)
                    }
                    MessageType.RECEIVED_BY_OPERATOR.valueType -> {
                        Log.d("ADAPTER_LIST_MESSAGE", "user message holder: type - RECEIVED_BY_OPERATOR, message - ${mData[position].message}")
                        holder.time.setCompoundDrawablesWithIntrinsicBounds(null, null, transformSizeDrawable(R.drawable.ic_db_check,
                            (15 * (inflater.context.getResources().displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
                        ), null)
                    }
                }
                // set color
                holder.message.setTextColor(mapAttr["color_text_user_message"] as Int)
                holder.time.setTextColor(mapAttr["color_time_mark"] as Int)
                holder.time.setDrawableColor(mapAttr["color_time_mark"] as Int)
                // set dimension
                holder.message.textSize = (mapAttr["size_user_message"] as Float)/scaleRatio
                holder.time.textSize = (mapAttr["size_time_mark"] as Float)/scaleRatio
                // set bg
                ViewCompat.setBackgroundTintList(holder.message, ColorStateList.valueOf(mapAttr["color_bg_user_message"] as Int))
            }
            0 -> {
                val holder = viewHolder as HolderSimpleServerMessageView
                // set content
                holder.message.text = messageObg.message
                holder.time.text = "${messageObg.operatorName} ${formatTime.format(messageObg.timestamp)}"
                messageObg.actions?.let {
                    holder.listActions.adapter = AdapterAction(inflater, it, mapAttr)
                }
                // set color
                holder.message.setTextColor(mapAttr["color_text_server_message"] as Int)
                holder.time.setTextColor(mapAttr["color_time_mark"] as Int)
                // set dimension
                holder.message.textSize = (mapAttr["size_server_message"] as Float)/scaleRatio
                holder.time.textSize = (mapAttr["size_time_mark"] as Float)/scaleRatio
                // set bg
                ViewCompat.setBackgroundTintList(holder.message, ColorStateList.valueOf(mapAttr["color_bg_server_message"] as Int))
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

    private fun TextView.setDrawableColor(color: Int) {
        compoundDrawables.filterNotNull().forEach {
            it.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }
}
