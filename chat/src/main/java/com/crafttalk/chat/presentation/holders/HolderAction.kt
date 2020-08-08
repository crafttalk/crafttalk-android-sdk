package com.crafttalk.chat.presentation.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R

class HolderAction(view: View): RecyclerView.ViewHolder(view) {
    val actionText: TextView = view.findViewById(R.id.action_text)
}
