package com.crafttalk.chat.presentation.base

import android.annotation.SuppressLint
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter <T : BaseItem> : RecyclerView.Adapter<RecyclerView.ViewHolder/*BaseViewHolder<T>*/>() {

    private val differCallback: DiffUtil.ItemCallback<T> = object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem.isSame(newItem)
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }
    }

    private val differ: AsyncListDiffer<T> = AsyncListDiffer(this, differCallback)

    open var data: List<T>
        get() = differ.currentList
        set(value) {
            differ.submitList(value.toList())
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as BaseViewHolder<T>).let {
            it.bindTo(getItem(it, position))
        }
    }

    override fun getItemCount() = data.size

    open fun getItem(holder: BaseViewHolder<T>, position: Int) = data[position]

    override fun getItemViewType(position: Int) = data[position].getLayout()

}