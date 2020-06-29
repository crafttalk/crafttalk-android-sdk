package com.crafttalk.chat.ui.file_viewer

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R

class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private var text: TextView = view.findViewById(R.id.description)
    private var icon: ImageView = view.findViewById(R.id.icon)

    fun bind(option: Option) {
        text.text = option.title
        icon.setImageDrawable(option.icon)
    }

}
