package com.crafttalk.chat.presentation.holders

import android.content.res.ColorStateList
import android.text.SpannableString
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.helper.extensions.*
import com.crafttalk.chat.presentation.model.WidgetMessageItem
import com.crafttalk.chat.utils.ChatAttr

class HolderOperatorWidgetMessage(
    val view: View,
    private val defaultWidgetView: View,
    private val getWidgetView: (widgetId: String) -> View?,
    private val findItemsViewOnWidget: (widgetId: String, widget: View, mapView: MutableMap<String, View>) -> Unit,
    private val bindWidget: (widgetId: String, message: SpannableString?, mapView: MutableMap<String, View>, payload: Any) -> Unit
) : BaseViewHolder<WidgetMessageItem>(view) {

    private val container: ViewGroup? = view.findViewById(R.id.widget_container)
    private val authorName: TextView? = view.findViewById(R.id.author_name)
    private val authorPreview: ImageView? = view.findViewById(R.id.author_preview)
    private val time: TextView? = view.findViewById(R.id.time)
    private val status: ImageView? = view.findViewById(R.id.status)
    private val date: TextView? = view.findViewById(R.id.date)

    private val messageFromDefaultWidget: TextView? by lazy { defaultWidgetView.findViewById(R.id.server_message) }
    private val contentContainerFromDefaultWidget: View? by lazy { defaultWidgetView.findViewById(R.id.content_container) }

    private val mapView: MutableMap<String, View> = mutableMapOf()
    private var widgetId: String? = null

    override fun bindTo(item: WidgetMessageItem) {
        if (widgetId == null) {
            val widgetView = getWidgetView(item.widgetId) ?: defaultWidgetView.apply {
                setBindDefaultWidget(item.message)
            }
            container?.addView(widgetView)
            findItemsViewOnWidget(item.widgetId, widgetView, mapView)
        } else if (widgetId != item.widgetId) {
            val widgetView = getWidgetView(item.widgetId) ?: defaultWidgetView.apply {
                setBindDefaultWidget(item.message)
            }
            mapView.clear()
            container?.removeAllViews()
            container?.addView(widgetView)
            findItemsViewOnWidget(item.widgetId, widgetView, mapView)
        }
        widgetId = item.widgetId
        bindWidget(item.widgetId, item.message, mapView, item.payload)
        commonBind(item)
    }

    private fun commonBind(item: WidgetMessageItem) {
        date?.setDate(item)
        // set content
        authorName?.setAuthor(item)
        authorPreview?.setAuthorIcon(item.authorPreview)
        time?.setTime(item)
        status?.setStatusMessage(item)
    }

    private fun setBindDefaultWidget(message: SpannableString?) {
        messageFromDefaultWidget?.setMessageText(
            textMessage = message,
            maxWidthTextMessage = ChatAttr.getInstance().widthItemOperatorTextMessage,
            colorTextMessage = ChatAttr.getInstance().colorTextOperatorMessage,
            colorTextLinkMessage = ChatAttr.getInstance().colorTextLinkOperatorMessage,
            sizeTextMessage = ChatAttr.getInstance().sizeTextOperatorMessage,
            resFontFamilyMessage = ChatAttr.getInstance().resFontFamilyOperatorMessage,
            isClickableLink = true,
            isSelectableText = true
        )
        // set bg
        contentContainerFromDefaultWidget?.apply {
            setBackgroundResource(ChatAttr.getInstance().bgOperatorMessageResId)
            ViewCompat.setBackgroundTintList(this, ColorStateList.valueOf(ChatAttr.getInstance().colorBackgroundOperatorMessage))
        }
    }
}