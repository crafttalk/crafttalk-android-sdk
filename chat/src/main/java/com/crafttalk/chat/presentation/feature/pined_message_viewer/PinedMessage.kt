package com.crafttalk.chat.presentation.feature.pined_message_viewer

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import kotlinx.android.synthetic.main.activity_pined_message.*

class PinedMessage : AppCompatActivity(),View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pined_message)
        pinned_message_image_navigate_back.setOnClickListener(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val headerAdapter = HeaderAdapterPinnedMessage()
        val recyclerView: RecyclerView = findViewById(R.id.pinned_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val concatAdapter = ConcatAdapter(headerAdapter)
        recyclerView.adapter = concatAdapter

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.pinned_message_image_navigate_back -> {
                finish()
            }
        }
    }
}