package com.crafttalk.chat.presentation.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R


class HolderUserFileMessage(view: View, val clickHandler: (fileUrl: String) -> Unit): RecyclerView.ViewHolder(view), View.OnClickListener {

    override fun onClick(view: View) {
        fileUrl?.let{
            clickHandler(it)
        }
    }

    val fileIcon: ImageView = view.findViewById(R.id.user_file)
    val time: TextView = view.findViewById(R.id.time)
    var fileUrl: String? = null

    init {
        view.setOnClickListener(this)
    }

}