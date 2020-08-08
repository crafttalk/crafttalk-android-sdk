package com.crafttalk.chat.presentation.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R

class HolderUserImageMessage(view: View, val clickHandler: (imageUrl: String, width: Int, height: Int) -> Unit): RecyclerView.ViewHolder(view), View.OnClickListener {

    override fun onClick(view: View) {
        imageUrl?.let{
            clickHandler(it, img.width, img.height)
        }
    }

    val img: ImageView = view.findViewById(R.id.user_image)
    val time: TextView = view.findViewById(R.id.time)
    var imageUrl: String? = null

    init {
        view.setOnClickListener(this)
    }

}