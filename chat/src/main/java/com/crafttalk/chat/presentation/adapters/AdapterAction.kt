package com.crafttalk.chat.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.domain.entity.message.Action
import com.crafttalk.chat.presentation.holders.HolderAction
import com.crafttalk.chat.utils.ChatAttr


class AdapterAction(
    private val inflater: LayoutInflater,
    private var mData: List<Action>,
    private val actionListener: AdapterListMessages.ActionListener
) : RecyclerView.Adapter<HolderAction>() {

    private val scaleRatio = inflater.context.resources.displayMetrics.density

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAction {
        return HolderAction(inflater.inflate(R.layout.item_action, parent, false))
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(viewHolder: HolderAction, position: Int) {
        // set content
        viewHolder.actionText.text = mData[position].actionText
        viewHolder.actionText.tag = mData[position].actionId
        // set color
        viewHolder.actionText.setTextColor(ChatAttr.mapAttr["color_text_server_action"] as Int)
        // set dimension
        viewHolder.actionText.textSize = (ChatAttr.mapAttr["size_server_action"] as Float)/scaleRatio
        // set bg
        if (mData.size == 1) {
            viewHolder.itemView.setBackgroundResource(R.drawable.background_single_item_action)
        } else {
            when (position) {
                0 -> viewHolder.itemView.setBackgroundResource(R.drawable.background_top_item_action)
                mData.size - 1 -> viewHolder.itemView.setBackgroundResource(R.drawable.background_bottom_item_action)
                else -> viewHolder.itemView.setBackgroundResource(R.drawable.background_item_action)
            }
        }
        viewHolder.itemView.setOnClickListener{
            actionListener.actionSelect(mData[position].actionId)
        }
    }

}
