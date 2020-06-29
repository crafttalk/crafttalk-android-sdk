package com.crafttalk.chat.ui.file_viewer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.iterator
import androidx.fragment.app.FragmentManager
import com.crafttalk.chat.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_file_viewer.*

@SuppressLint("RestrictedApi")
class BottomSheetFileViewer : BottomSheetDialogFragment() {

    companion object {
        private const val KEY_MENU = "menu"

        private fun newInstance(builder: Builder): BottomSheetFileViewer {
            val fragment = BottomSheetFileViewer()
            val args = Bundle()
            args.putInt(KEY_MENU, builder.menuRes ?: R.menu.options)
            fragment.arguments = args
            fragment.listener = builder.listener
            return fragment
        }
    }

    private lateinit var adapter: Adapter
    private var listener: Listener? = null
    private val menuInflater by lazy {
        MenuInflater(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_file_viewer, container, false)
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val arguments = arguments ?: throw IllegalStateException("You need to create this via the builder")

        val options = mutableListOf<Option>()
        inflate(arguments.getInt(KEY_MENU), options)

        adapter = Adapter {
            listener?.onModalOptionSelected(this@BottomSheetFileViewer.tag, it)
            dismissAllowingStateLoss()
        }
        list.adapter = adapter

        adapter.setData(options)
        listener = bindHost()
    }

    private fun inflate(menuRes: Int, options: MutableList<Option>) {
        val menu = MenuBuilder(context)
        menuInflater.inflate(menuRes, menu)
        for (item in menu.iterator()) {
            val option = Option(item.itemId, item.title, item.icon)
            options.add(option)
        }
    }

    private fun bindHost(): Listener {
        if (listener == null) {
            if (parentFragment != null) {
                if (parentFragment is Listener) {
                    return parentFragment as Listener
                }
            }
            if (context is Listener) {
                return context as Listener
            }
            throw IllegalStateException("BottomSheetFileViewer must be attached to a parent (activity or fragment) that implements the BottomSheetFileViewer.Listener")
        }
        else {
            return listener as Listener
        }
    }

    class Builder {
        var menuRes: Int? = null
        var listener: Listener? = null

        fun add(@MenuRes menuRes: Int): Builder {
            this.menuRes = menuRes
            return this
        }

        fun setListener(listener: Listener): Builder {
            this.listener = listener
            return this
        }

        fun show(fragmentManager: FragmentManager): BottomSheetFileViewer {
            val dialog = newInstance(this)
            dialog.show(fragmentManager, dialog.tag)
            return dialog
        }
    }

    interface Listener {
        fun onModalOptionSelected(tag: String?, option: Option)
    }

}