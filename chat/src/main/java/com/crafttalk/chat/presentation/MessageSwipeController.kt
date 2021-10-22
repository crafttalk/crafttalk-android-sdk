package com.crafttalk.chat.presentation

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.RecyclerView
import com.crafttalk.chat.R
import com.crafttalk.chat.presentation.adapters.AdapterListMessages
import com.crafttalk.chat.presentation.model.MessageModel
import com.crafttalk.chat.utils.ChatAttr
import kotlin.math.abs

class MessageSwipeController(
    private val swipeControllerAction: (MessageModel) -> Unit
): ItemTouchHelper.Callback() {

    private var isReturnBackToStartPosition = false
    private var isStartedVibrate = false
    private var widthItem: Float = 0f

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
//        return makeMovementFlags(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.LEFT)
        return makeFlag(ACTION_STATE_IDLE, ItemTouchHelper.LEFT) or makeFlag(ACTION_STATE_SWIPE, ItemTouchHelper.LEFT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//        Log.d("TEST_SWIPED", "onSwiped;")
    }

    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        widthItem = viewHolder.itemView.width.toFloat()

        val heightItemView = viewHolder.itemView.height.toFloat()
        val heightIncludeDate: Float = viewHolder.itemView.findViewById<ViewGroup>(R.id.include_date)?.height?.toFloat() ?: DEFAULT_HEIGHT_NPE_VIEW_GROUP
        val heightIncludeMessageInfo: Float = viewHolder.itemView.findViewById<ViewGroup>(R.id.include_message_info)?.height?.toFloat() ?: DEFAULT_HEIGHT_NPE_VIEW_GROUP

        if (
            actionState == ACTION_STATE_SWIPE &&
            viewHolder.itemView.y + canvas.height.toFloat() - heightIncludeMessageInfo > 0 &&
            viewHolder.itemView.y + heightIncludeDate < canvas.height.toFloat()
        ) {
            setTouchListener(recyclerView, viewHolder)
            val alpha = 1.0f - abs(dX) / widthItem
            if (widthItem * 0.1 > abs(dX)) {
                isStartedVibrate = false
            }
            if (widthItem * 0.2 > abs(dX)) {
                viewHolder.itemView.alpha = alpha
                viewHolder.itemView.translationX = dX
            }
            if (widthItem * 0.1 <= abs(dX)) {
                vibrate(viewHolder.itemView)
                drawReplyButton(
                    canvas = canvas,
                    itemTop = viewHolder.itemView.y,
                    itemBottom = viewHolder.itemView.y + heightItemView,
                    contentTop = viewHolder.itemView.y + heightIncludeDate,
                    contentBottom = viewHolder.itemView.y + heightItemView - heightIncludeMessageInfo
                )
            }
        }
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (isReturnBackToStartPosition) {
            isReturnBackToStartPosition = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        recyclerView.setOnTouchListener { _, event ->
            isReturnBackToStartPosition = event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
            if (isReturnBackToStartPosition && abs(viewHolder.itemView.translationX) >= widthItem * 0.1) {
                val adapter = recyclerView.adapter as AdapterListMessages
                adapter.currentList?.get(viewHolder.adapterPosition)?.let(swipeControllerAction)
            }
            false
        }
    }

    private fun vibrate(view: View) {
        if (isStartedVibrate) return
        view.performHapticFeedback(
            HapticFeedbackConstants.KEYBOARD_TAP,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
        isStartedVibrate = true
    }

    private fun drawReplyButton(
        canvas: Canvas,
        itemTop: Float,      // A
        itemBottom: Float,   // D
        contentTop: Float,   // B
        contentBottom: Float // C
    ) {

        val recyclerViewHeight = canvas.height.toFloat()

        val iconMiddle: Float = when {
            itemTop > 0f && itemBottom < recyclerViewHeight -> (contentTop + contentBottom) / 2
            itemTop > 0f && contentTop < recyclerViewHeight && contentBottom > recyclerViewHeight -> (contentTop + recyclerViewHeight) / 2
            itemTop > 0f && contentBottom < recyclerViewHeight && itemBottom > recyclerViewHeight -> (contentTop + contentBottom) / 2
            contentTop < 0f && contentBottom > 0f && itemBottom < recyclerViewHeight -> contentBottom / 2
            itemTop < 0f && contentTop > 0f && itemBottom < recyclerViewHeight -> (contentTop + contentBottom) / 2
            contentTop == 0f && itemBottom < recyclerViewHeight -> contentBottom / 2
            itemTop > 0f && contentBottom == recyclerViewHeight -> (contentTop + recyclerViewHeight) / 2
            else -> null
        } ?: return

        ChatAttr.getInstance().drawableReplyMessageIcon?.let { icon ->
            val halfHeightIcon = icon.intrinsicWidth.toFloat() / 2f
            icon.setBounds(
                canvas.width - icon.intrinsicWidth - INDENT_TO_RIGHT_OF_QUOTE_ICON,
                (iconMiddle - halfHeightIcon).toInt(),
                canvas.width - INDENT_TO_RIGHT_OF_QUOTE_ICON,
                (iconMiddle + halfHeightIcon).toInt()
            )
            icon.draw(canvas)
        }
    }

    companion object {
        const val INDENT_TO_RIGHT_OF_QUOTE_ICON = 80
        const val DEFAULT_HEIGHT_NPE_VIEW_GROUP = 0f
    }

}