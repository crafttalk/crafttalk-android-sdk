package com.crafttalk.chat.ui.file_viewer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R

class Adapter(private val callback: (option: Option) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val data = mutableListOf<Option>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bottom_sheet_file_viewer_item, parent, false)
        val holder = ItemViewHolder(view)
        view.setOnClickListener {
            val localData = data[holder.adapterPosition]
            callback.invoke(localData)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val option = data[position]
            holder.bind(option)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setData(options: List<Option>) {
        this.data.clear()
        this.data.addAll(options)
        notifyDataSetChanged()
    }

}