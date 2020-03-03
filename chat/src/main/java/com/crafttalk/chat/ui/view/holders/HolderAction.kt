package com.crafttalk.chat.ui.view.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.data.remote.socket_service.SocketAPI

class HolderAction(view: View): RecyclerView.ViewHolder(view) {
    val actionText: TextView = view.findViewById(R.id.action_text)

    init {
        view.setOnClickListener {
            SocketAPI.selectAction(actionText.tag.toString())
        }
    }
}
