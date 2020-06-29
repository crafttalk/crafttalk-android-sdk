package com.crafttalk.chat.ui.chat_view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.marginTop
import com.crafttalk.chat.R
import com.crafttalk.chat.utils.convertDpToPx
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.bottom_sheet_show_image.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShowImageDialog(activity: Activity): Dialog(activity), View.OnClickListener {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onClick(view: View) {
        when(view.id) {
            R.id.show_image -> {

            }
            else -> dismiss()
        }
    }

    var url: String? = null

    companion object {
        private fun newInstance(builder: Builder): ShowImageDialog {
            val dialog = ShowImageDialog(builder.activity)
            builder.imageUrl?.let {
                dialog.url = it
            }
            return dialog
        }
    }

    class Builder(val activity: Activity) {
        var imageUrl: String? = null

        fun setUrl(url: String): Builder {
            this.imageUrl = url
            return this
        }

        fun show() {
            Log.d("ShowImageBottomDialog", "show")
            if (imageUrl != null) {
                val dialog = newInstance(this)
                dialog.show()
            }
        }

    }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.bottom_sheet_show_image)
        this.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        url?.let {

            scope.launch {
                try {
                    val bitmapImage = Picasso.with(context).load(it).get()


                    show_image.background = null

                    val (widthInPx, heightInPx) = let {
                        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
                        val size = Point()
                        display.getSize(size)
                        Pair(size.x, size.y)
                    }

                    Log.d("ShowImageBottomDialog", "show_image.marginTop = ${bitmapImage.width}; ${bitmapImage.height};;;;${show_image.marginTop}; ${convertDpToPx(20f, context)}")

                    show_image.post {
                        bitmapImage?.let {bitmap ->
                            val (newWidthImage, newHeightImage) = resizeImageToSizeWindow(
                                bitmap.width.toFloat(),
                                bitmap.height.toFloat(),
                                widthInPx.toFloat(),
                                heightInPx.toFloat(),
                                show_image.marginTop.toFloat()
                            )

                            show_image.layoutParams.let {
                                it.width = newHeightImage.toInt()
                                it.height = newWidthImage.toInt()
                            }
                            show_image.setImageBitmap(Bitmap.createScaledBitmap(
                                bitmap,
                                newWidthImage.toInt(),
                                newHeightImage.toInt(),
                                false
                            ))
                        }
                    }
                }
                catch (allEx: Exception) {
                    show_image.post {
                        show_image.layoutParams.let {
                            it.height = 200
                            it.width = 200
                        }
                        show_image.setBackgroundColor(R.color.default_color_company)
                    }
                    Log.d("Ex", "fail; ${allEx.message}; ${allEx.stackTrace}")
                }
            }
        }

        show_image.setOnClickListener(this)
    }

}


// all param in px
fun resizeImageToSizeWindow(widthImage: Float, heightImage: Float, widthScreen: Float, heightScreen: Float, margin: Float): Pair<Float, Float> {
    Log.d("resizeImageToSizeWindow", "0) ${widthImage}, ${heightImage}")
    var newWidthImage: Float
    var newHeightImage: Float
    if (widthImage > heightImage) {
        newWidthImage = widthScreen - 2 * margin
        newHeightImage = (heightImage / widthImage) * newWidthImage
        Log.d("resizeImageToSizeWindow", "1) ${newWidthImage}, ${newHeightImage}")
        if (newHeightImage > heightScreen - 4 * margin) {
            newHeightImage = heightScreen - 4 * margin
            newWidthImage = (widthImage / heightImage) * newHeightImage
            Log.d("resizeImageToSizeWindow", "2) ${newWidthImage}, ${newHeightImage}")
        }
    }
    else {
        newHeightImage = heightScreen - 2 * margin
        newWidthImage = (widthImage / heightImage) * newHeightImage
        Log.d("resizeImageToSizeWindow", "3) ${newWidthImage}, ${newHeightImage}")
        if (newWidthImage > widthScreen - 8 * margin) {
            newWidthImage = widthScreen - 8 * margin
            newHeightImage = (heightImage / widthImage) * newWidthImage
            Log.d("resizeImageToSizeWindow", "4) ${newWidthImage}, ${newHeightImage}")
        }
    }

    return Pair(newWidthImage, newHeightImage)
}
