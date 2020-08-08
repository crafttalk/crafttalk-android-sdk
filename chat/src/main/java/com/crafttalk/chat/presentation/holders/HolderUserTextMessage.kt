package com.crafttalk.chat.presentation.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R

class HolderUserTextMessage(view: View): RecyclerView.ViewHolder(view) {
    val message: TextView = view.findViewById(R.id.user_message)
    val time: TextView = view.findViewById(R.id.time)
}
