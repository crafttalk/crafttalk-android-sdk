package com.crafttalk.chat.ui.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.data.remote.pojo.Action
import com.crafttalk.chat.ui.view.holders.HolderAction


class AdapterAction(private val inflater: LayoutInflater, private var mData: Array<Action>, private val mapAttr: Map<String, Any>) : RecyclerView.Adapter<HolderAction>() {

    private val scaleRatio = inflater.context.resources.displayMetrics.density

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAction {
        return HolderAction(inflater.inflate(R.layout.item_action, parent, false))
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun onBindViewHolder(viewHolder: HolderAction, position: Int) {
        // set content
        viewHolder.actionText.text = mData[position].action_text
        viewHolder.actionText.tag = mData[position].action_id
        // set color
        viewHolder.actionText.setTextColor(mapAttr["color_text_server_action"] as Int)
        // set dimension
        viewHolder.actionText.textSize = (mapAttr["size_server_action"] as Float)/scaleRatio
        // set bg
        if (mData.size == 1) {
            viewHolder.itemView.setBackgroundResource(R.drawable.background_single_item_action)
        }else {
            if (position == 0) {
                viewHolder.itemView.setBackgroundResource(R.drawable.background_top_item_action)
            }
            else if (position == mData.size - 1) {
                viewHolder.itemView.setBackgroundResource(R.drawable.background_bottom_item_action)
            }
        }
    }

}
