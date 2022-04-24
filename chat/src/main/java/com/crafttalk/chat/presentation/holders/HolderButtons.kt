package com.crafttalk.chat.presentation.holders

import android.content.Intent
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.message.NetworkButtonOperation
import com.crafttalk.chat.presentation.base.BaseViewHolder
import com.crafttalk.chat.presentation.model.ButtonItem
import com.crafttalk.chat.presentation.model.ButtonsListItem
import com.crafttalk.chat.utils.ChatAttr

class HolderButtons(
    val view: View,
    private val hasSelectedButton: Boolean,
    private val clickHandler: (actionId: String, buttonId: String) -> Unit
) : BaseViewHolder<ButtonsListItem>(view) {

    private val buttonsContainer: ViewGroup? = view.findViewById(R.id.item_buttons)

    override fun bindTo(item: ButtonsListItem) {
        item.buttons.forEach {
            buttonsContainer?.addView(
                createAndBindButton(it)
            )
        }
    }

    private fun createAndBindButton(item: ButtonItem): View {
        val itemButton = LayoutInflater.from(view.context).inflate(R.layout.com_crafttalk_chat_item_button, null, false)
        val buttonText: TextView? = itemButton.findViewById(R.id.button_text)

        // set width item
        val params = LinearLayout.LayoutParams(
            item.width,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(
            item.marginStart,
            item.marginTop,
            item.marginEnd,
            item.marginBottom
        )
        itemButton?.layoutParams = params

        buttonText?.apply {
            // set content
            text = item.text
            // set color
            setTextColor(item.textColor)
            // set dimension
            setTextSize(TypedValue.COMPLEX_UNIT_PX, ChatAttr.getInstance().sizeTextOperatorButton)
            // set font
            ChatAttr.getInstance().resFontFamilyOperatorButton?.let {
                typeface = ResourcesCompat.getFont(context, it)
            }
        }
        // set bg
        itemButton?.apply {
            setBackgroundResource(item.backgroundRes)
        }

        itemButton.setOnClickListener {
            if (hasSelectedButton) return@setOnClickListener
            if (item.action.isEmpty()) return@setOnClickListener
            if (item.id.isEmpty()) return@setOnClickListener

            when (item.typeOperation) {
                NetworkButtonOperation.URL -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.action))
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    view.context.startActivity(intent)
                }
                NetworkButtonOperation.ACTION -> {
                    clickHandler(item.action, item.id)
                }
            }
        }

        return itemButton
    }

}