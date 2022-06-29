package com.crafttalk.sampleChat.widgets.carousel

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.crafttalk.sampleChat.R

data class CarouselWidget(
    val items: List<ItemCarouselWidget>
)

data class ItemCarouselWidget(
    val title: String,
    val subtitle: String,
    val image_url: String,
    val actions: List<ItemActionCarouselWidget>?
)

data class ItemActionCarouselWidget(
    val action_id: String,
    val action_type: String,
    val action_title: String
)

@SuppressLint("InflateParams")
fun createCarouselWidget(inflater: LayoutInflater): View {
    return inflater.inflate(R.layout.widget_carousel, null, false)
}

fun bindCarouselWidget(
    inflater: LayoutInflater,
    listView: ViewGroup,
    data: CarouselWidget,
    clickButtonOnWidget: (actionId: String) -> Unit
) {
    listView.removeAllViews()
    data.items.forEach { itemCarouselWidget ->
        val itemView = inflater.inflate(R.layout.item_widget_carousel, listView, false)
        itemView.findViewById<ImageView>(R.id.item_carousel_img).apply {
            Glide.with(context)
                .load(itemCarouselWidget.image_url)
                .into(this)
        }
        itemView.findViewById<TextView>(R.id.item_carousel_title).apply {
            text = itemCarouselWidget.title
        }
        itemView.findViewById<TextView>(R.id.item_carousel_subtitle).apply {
            text = itemCarouselWidget.subtitle
        }
        itemView.findViewById<ViewGroup>(R.id.item_actions).apply {
            itemCarouselWidget.actions?.forEach loopActions@ { itemActionCarouselWidget ->
                val actionView = (inflater.inflate(R.layout.item_action_widget_carousel, this, false) as? TextView) ?: return@loopActions
                actionView.text = itemActionCarouselWidget.action_title
                actionView.setOnClickListener {
                    if (itemActionCarouselWidget.action_type == "url") {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(itemActionCarouselWidget.action_id))
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    } else {
                        clickButtonOnWidget(itemActionCarouselWidget.action_id)
                    }
                }
                this.addView(actionView)
            }
        }
        listView.addView(itemView)
    }
}