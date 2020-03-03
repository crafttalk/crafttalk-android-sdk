package com.crafttalk.chat.ui.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.ui.view.holders.HolderAction
import com.crafttalk.chat.data.remote.Action


class AdapterAction(private val inflater: LayoutInflater, private var mData: Array<Action>, val mapAttr: Map<String, Any>) : RecyclerView.Adapter<HolderAction>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAction {
        return HolderAction(inflater.inflate(R.layout.item_action, parent, false))
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(viewHolder: HolderAction, position: Int) {
        viewHolder.actionText.text = mData[position].action_text
        viewHolder.actionText.tag = mData[position].action_id
        viewHolder.actionText.setTextColor(mapAttr["color_main"] as Int)

        if (mData.size == 1) {
            viewHolder.itemView.setBackgroundResource(R.drawable.background_single_item_action)
        }else {
            if (position == 0) {
                viewHolder.itemView.setBackgroundResource(R.drawable.background_top_item_action)
            }
            else if (position == mData.size - 1) {
                // check 19 version
                viewHolder.itemView.setBackgroundResource(R.drawable.background_bottom_item_action)
            }
        }

    }

}
