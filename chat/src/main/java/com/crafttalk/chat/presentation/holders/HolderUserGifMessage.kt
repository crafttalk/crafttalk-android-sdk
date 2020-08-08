package com.crafttalk.chat.presentation.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R

class HolderUserGifMessage(view: View, val clickHandler: (gifUrl: String, width: Int, height: Int) -> Unit): RecyclerView.ViewHolder(view), View.OnClickListener {

    override fun onClick(view: View) {
        clickHandler(gifUrl, gif.width, gif.height)
    }

    val gif: ImageView = view.findViewById(R.id.server_image)
    val time: TextView = view.findViewById(R.id.time)
    lateinit var gifUrl: String

    init {
        view.setOnClickListener(this)
    }

}