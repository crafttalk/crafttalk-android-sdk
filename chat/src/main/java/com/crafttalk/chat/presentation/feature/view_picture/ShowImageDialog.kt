package com.crafttalk.chat.presentation.feature.view_picture

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.marginTop
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.file.TypeFile
import com.crafttalk.chat.domain.entity.file.TypeFile.*
import kotlinx.android.synthetic.main.bottom_sheet_show_image.*

class ShowImageDialog(activity: Activity): Dialog(activity), View.OnClickListener {

    override fun onClick(view: View) {
        when(view.id) {
            R.id.show_image -> {}
            else -> dismiss()
        }
    }

    private lateinit var url: String
    private var width: Int = 0
    private var height: Int = 0
    private lateinit var type: TypeFile

    companion object {
        private fun newInstance(builder: Builder): ShowImageDialog {
            val dialog = ShowImageDialog(builder.activity)
            dialog.url = builder.imageUrl!!
            dialog.width = builder.width!!
            dialog.height = builder.height!!
            dialog.type = builder.type!!
            return dialog
        }
    }

    class Builder(val activity: Activity) {
        var imageUrl: String? = null
        var width: Int? = null
        var height: Int? = null
        var type: TypeFile? = null

        fun setUrl(url: String): Builder {
            this.imageUrl = url
            return this
        }

        fun setSize(width: Int, height: Int): Builder {
            this.width = width
            this.height = height
            return this
        }

        fun setType(type: TypeFile): Builder {
            this.type = type
            return this
        }

        fun show() {
            imageUrl ?: return
            width ?: return
            height ?: return
            type ?: return
            newInstance(this).show()
        }

    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.bottom_sheet_show_image)
        this.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val (widthInPx, heightInPx) = let {
            val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val size = Point()
            display.getSize(size)
            Pair(size.x, size.y)
        }

        val (newWidthImage, newHeightImage) = resizeImageToSizeWindow(
            width,
            height,
            widthInPx,
            heightInPx,
            show_image.marginTop
        )

        show_image.background = null

        when (type) {
            IMAGE -> {
                Glide.with(context)
                    .load(url)
                    .error(R.color.default_color_company)
                    .apply(RequestOptions().override(newWidthImage, newHeightImage))
                    .into(show_image)
            }
            GIF -> {
                Glide.with(context)
                    .asGif()
                    .load(url)
                    .error(R.color.default_color_company)
                    .apply(RequestOptions().override(newWidthImage, newHeightImage))
                    .into(show_image)
            }
            FILE -> TODO()
        }

        show_image.setOnClickListener(this)
    }

}


// all param in px
fun resizeImageToSizeWindow(widthImage: Int, heightImage: Int, widthScreen: Int, heightScreen: Int, margin: Int): Pair<Int, Int> {
    Log.d("resizeImageToSizeWindow", "0) ${widthImage}, $heightImage")
    var newWidthImage: Int
    var newHeightImage: Int
    if (widthImage > heightImage) {
        newWidthImage = widthScreen - 2 * margin
        newHeightImage = (heightImage / widthImage) * newWidthImage
        Log.d("resizeImageToSizeWindow", "1) ${newWidthImage}, $newHeightImage")
        if (newHeightImage > heightScreen - 4 * margin) {
            newHeightImage = heightScreen - 4 * margin
            newWidthImage = (widthImage / heightImage) * newHeightImage
            Log.d("resizeImageToSizeWindow", "2) ${newWidthImage}, $newHeightImage")
        }
    }
    else {
        newHeightImage = heightScreen - 2 * margin
        newWidthImage = (widthImage / heightImage) * newHeightImage
        Log.d("resizeImageToSizeWindow", "3) ${newWidthImage}, $newHeightImage")
        if (newWidthImage > widthScreen - 8 * margin) {
            newWidthImage = widthScreen - 8 * margin
            newHeightImage = (heightImage / widthImage) * newWidthImage
            Log.d("resizeImageToSizeWindow", "4) ${newWidthImage}, $newHeightImage")
        }
    }

    return Pair(newWidthImage, newHeightImage)
}