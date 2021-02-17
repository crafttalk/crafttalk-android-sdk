package com.crafttalk.chat.presentation.base

import android.annotation.SuppressLint
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapterWithPagination <T : BaseItem>(
    private val differCallback: DiffUtil.ItemCallback<T>
) : PagedListAdapter<T, RecyclerView.ViewHolder>(differCallback) {

    constructor() : this(
        object : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
                return oldItem.isSame(newItem)
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
                return oldItem == newItem
            }
        }
    )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as BaseViewHolder<T>).let {
            val messageItem = getItem(position)
            if (messageItem == null) {
//            holder.clear()
            } else {
                it.bindTo(messageItem)
            }
        }
    }

    override fun getItemViewType(position: Int) = getItem(position)?.getLayout() ?: -1

}