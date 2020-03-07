package com.crafttalk.chat.ui.view.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R

class HolderSimpleServerMessageView(view: View): RecyclerView.ViewHolder(view) {
    val message: TextView = view.findViewById(R.id.server_message)
    val listActions: RecyclerView = view.findViewById(R.id.actions_list)
    val time: TextView = view.findViewById(R.id.time)
}