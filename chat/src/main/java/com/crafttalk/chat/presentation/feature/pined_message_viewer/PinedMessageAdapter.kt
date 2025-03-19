package com.crafttalk.chat.presentation.feature.pined_message_viewer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R

class PinedMessageAdapter {
    class PinedMessageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun onCreateViewHolder(parent: ViewGroup): PinedMessageViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.pinned_message_header, parent,false)
            return PinedMessageViewHolder(view)
        }
    }
}