package com.crafttalk.chat.ui.view.adapters

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.data.local.db.entity.Message
import com.crafttalk.chat.data.model.MessageType
import com.crafttalk.chat.ui.view.holders.HolderSimpleServerMessageView
import com.crafttalk.chat.ui.view.holders.HolderSimpleUserMessageView
import java.text.SimpleDateFormat
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat


class AdapterListMessages(private val inflater: LayoutInflater, private var mData: List<Message>, private val mapAttr: Map<String, Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

                when (messageObg.messageType) {
                    MessageType.VISITOR_MESSAGE.valueType -> {}
                    MessageType.RECEIVED_BY_MEDIATO.valueType -> {
                        Log.d("ADAPTER_LIST_MESSAGE", "user message holder: type - RECEIVED_BY_MEDIATO, message - ${mData[position].message}")
                        holder.time.setCompoundDrawablesWithIntrinsicBounds(null, null, transformSizeDrawable(R.drawable.ic_check, 20), null)
                    }
                    MessageType.RECEIVED_BY_OPERATOR.valueType -> {
                        Log.d("ADAPTER_LIST_MESSAGE", "user message holder: type - RECEIVED_BY_OPERATOR, message - ${mData[position].message}")
                        holder.time.setCompoundDrawablesWithIntrinsicBounds(null, null, transformSizeDrawable(R.drawable.ic_db_check, 24), null)
                    }
                }

                holder.message.text = messageObg.message

                val newMainColor = mapAttr["color_main"] as Int
                val bgMessageUser = DrawableCompat.wrap(ContextCompat.getDrawable(inflater.context, R.drawable.background_item_simple_user_message)!!)
                holder.message.setBackgroundDrawable(bgMessageUser) // in version 19 drop padding
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    DrawableCompat.setTint(bgMessageUser, newMainColor)
                }
                else {
                    bgMessageUser.mutate().setColorFilter(newMainColor, PorterDuff.Mode.SRC_IN)
                }

                holder.time.text = "${messageObg.operatorName} ${formatTime.format(messageObg.timestamp)}"
            }
            0 -> {
                val holder = viewHolder as HolderSimpleServerMessageView
                holder.message.text = messageObg.message
                holder.time.text = "${messageObg.operatorName} ${formatTime.format(messageObg.timestamp)}"
                messageObg.actions?.let {
                    holder.listActions.adapter = AdapterAction(inflater, it, mapAttr)
                }
            }
        }
    }

    private fun transformSizeDrawable(idIcon: Int, newSize: Int): Drawable {
        val dr = ResourcesCompat.getDrawable(inflater.context.resources, idIcon, null)
        val bitmap = (dr as BitmapDrawable).bitmap
        return BitmapDrawable(inflater.context.resources, Bitmap.createScaledBitmap(bitmap, newSize, newSize, true))
    }

}
