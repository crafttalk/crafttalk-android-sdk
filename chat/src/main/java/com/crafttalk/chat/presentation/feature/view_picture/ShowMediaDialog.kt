package com.crafttalk.chat.presentation.feature.view_picture

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.file.TypeFile.*
import kotlinx.android.synthetic.main.bottom_sheet_show_gif.*
import kotlinx.android.synthetic.main.bottom_sheet_show_image.*

class ShowImageDialog(
    activity: Activity,
    style: Int
): Dialog(activity, style), View.OnClickListener {

    override fun onClick(view: View) {
        when(view.id) {
            R.id.image_navigate_back, R.id.gif_navigate_back -> dismiss()
        }
    }

    private var url: String? = null
    private var type: TypeFile? = null

    companion object {
        private fun newInstance(builder: Builder): ShowImageDialog {
            val dialog = ShowImageDialog(builder.activity, R.style.ThemeFullscreen)
            dialog.url = builder.imageUrl!!
            dialog.type = builder.type!!
            return dialog
        }
    }

    class Builder(val activity: Activity) {
        var imageUrl: String? = null
        var type: TypeFile? = null

        fun setUrl(url: String): Builder {
            this.imageUrl = url
            return this
        }

        fun setType(type: TypeFile): Builder {
            this.type = type
            return this
        }

        fun show() {
            imageUrl ?: return
            type ?: return
            newInstance(this).show()
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (type) {
            IMAGE -> {
                setContentView(R.layout.bottom_sheet_show_image)
                image_navigate_back.setOnClickListener(this)
                Glide.with(context)
                    .load(url)
                    .error(R.drawable.background_item_media_message_placeholder)
                    .into(image_show)
            }
            GIF -> {
                setContentView(R.layout.bottom_sheet_show_gif)
                gif_navigate_back.setOnClickListener(this)
                Glide.with(context)
                    .asGif()
                    .load(url)
                    .error(R.drawable.background_item_media_message_placeholder)
                    .into(gif_show)
            }
            else -> {}
        }

    }

}