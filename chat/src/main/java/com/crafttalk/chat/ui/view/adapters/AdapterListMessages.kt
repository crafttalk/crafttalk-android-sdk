package com.crafttalk.chat.ui.view.adapters

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.data.local.db.entity.Message
import com.crafttalk.chat.ui.view.holders.SimpleServerMessageViewHolder
import com.crafttalk.chat.ui.view.holders.SimpleUserMessageViewHolder
import java.text.SimpleDateFormat


class AdapterListMessages(private val inflater: LayoutInflater, private var mData: List<Message>, val mapAttr: Map<String, Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @SuppressLint("SimpleDateFormat")
    val formatTime = SimpleDateFormat("dd.MM.yyyy HH:mm")

    fun setData(newData: List<Message>) {
        mData = newData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            -1 -> {
                val view = inflater.inflate(R.layout.item_user_message, parent, false)
                SimpleUserMessageViewHolder(view)
            }
            0 -> {
                val view = inflater.inflate(R.layout.item_server_message, parent, false)
                SimpleServerMessageViewHolder(view)
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

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder.itemViewType) {
            -1 -> {
                val messageObg = mData[position]
                val holder = viewHolder as SimpleUserMessageViewHolder
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

                holder.time.text = "Вы ${formatTime.format(messageObg.timestamp)}"
            }
            0 -> {
                val messageObg = mData[position]
                val holder = viewHolder as SimpleServerMessageViewHolder
                holder.message.text = messageObg.message
                holder.time.text = "Бот ${formatTime.format(messageObg.timestamp)}"
                messageObg.actions?.let {
                    holder.listActions.adapter = AdapterAction(inflater, it, mapAttr)
                }
            }
        }
    }

}
