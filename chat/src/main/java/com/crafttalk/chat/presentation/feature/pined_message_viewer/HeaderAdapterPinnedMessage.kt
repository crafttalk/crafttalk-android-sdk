package com.crafttalk.chat.presentation.feature.pined_message_viewer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R

class HeaderAdapterPinnedMessage: RecyclerView.Adapter<HeaderAdapterPinnedMessage.HeaderViewHolder>() {
    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pinned_message_header, parent, false)
        return HeaderViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {

    }
}