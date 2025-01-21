package com.example.tempnavigation.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.max

abstract class SwipeHelper(
    private val recyclerView: RecyclerView
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.ACTION_STATE_IDLE,
    ItemTouchHelper.LEFT
) {
    private val TAG = "SwipeHelper"
    private var swipedPosition = -1
    private val buttonsBuffer: MutableMap<Int, List<UnderlayButton>> = mutableMapOf()
    private val recoverQueue = object : LinkedList<Int>() {
        override fun add(element: Int): Boolean {
            if (contains(element)) return false
            return super.add(element)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private val touchListener = View.OnTouchListener { _, event ->
        if (swipedPosition < 0) return@OnTouchListener false
        val itemView = recyclerView.findViewHolderForAdapterPosition(swipedPosition)?.itemView
        itemView?.let {
            val swipedRect = RectF(
                it.left.toFloat(),
                it.top.toFloat(),
                it.right.toFloat(),
                it.bottom.toFloat()
            )
            buttonsBuffer[swipedPosition]?.forEach { button ->
                button.handle(event, swipedRect)
            }
        }
        recoverQueue.add(swipedPosition)
        swipedPosition = -1
        recoverSwipedItem()
        true
    }

    init {
        recyclerView.setOnTouchListener(touchListener)
    }

    private fun recoverSwipedItem() {
        while (!recoverQueue.isEmpty) {
            val position = recoverQueue.poll() ?: continue
            recyclerView.adapter?.notifyItemChanged(position)
            buttonsBuffer.remove(position)
        }
    }

    fun invalidateCustomButton(position: Int) {
        buttonsBuffer.remove(position)
    }

    private fun drawButtons(
        canvas: Canvas,
        buttons: List<UnderlayButton>,
        itemView: View,
        dX: Float
    ) {
        var right = itemView.right
        buttons.forEach { button ->
            val width = button.intrinsicWidth
            val left = right - width
            button.draw(
                canvas,
                RectF(left, itemView.top.toFloat(), right.toFloat(), itemView.bottom.toFloat())
            )
            right = left.toInt()
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val position = viewHolder.adapterPosition
        val itemView = viewHolder.itemView
        var maxDx = dX

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
            if (!buttonsBuffer.containsKey(position)) {
                buttonsBuffer[position] = instantiateUnderlayButton(position)
            }
            val buttons = buttonsBuffer[position] ?: return
            if (buttons.isEmpty()) return
            maxDx = max(-buttons.intrinsicWidth(), dX)
            drawButtons(c, buttons, itemView, maxDx)
        }

        super.onChildDraw(c, recyclerView, viewHolder, maxDx, dY, actionState, isCurrentlyActive)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        if (position == RecyclerView.NO_POSITION) {
            return // Prevent further execution if the position is invalid
        }
        if (swipedPosition != position) {
            recoverQueue.add(swipedPosition)
        }
        swipedPosition = position
        recoverSwipedItem()
    }

    abstract fun instantiateUnderlayButton(position: Int): List<UnderlayButton>

    //region UnderlayButton
    interface UnderlayButtonClickListener {
        fun onClick()
    }

    class UnderlayButton(
        private val context: Context,
        private val title: String,
        textSize: Float,
        @ColorRes private val colorRes: Int,
        private val clickListener: UnderlayButtonClickListener
    ) {
        private var clickableRegion: RectF? = null
        private val textSizeInPixel: Float = textSize * context.resources.displayMetrics.density // dp to px
        private val horizontalPadding = 50.0f
        val intrinsicWidth: Float

        init {
            val paint = Paint()
            paint.textSize = textSizeInPixel
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textAlign = Paint.Align.LEFT
            val titleBounds = Rect()
            paint.getTextBounds(title, 0, title.length, titleBounds)
            intrinsicWidth = titleBounds.width() + 2 * horizontalPadding
        }

        fun draw(canvas: Canvas, rect: RectF) {
            val paint = Paint()

            // Draw background
            paint.color = ContextCompat.getColor(context, colorRes)
            canvas.drawRect(rect, paint)

            // Draw title
            paint.color = ContextCompat.getColor(context, android.R.color.white)
            paint.textSize = textSizeInPixel
            paint.typeface = Typeface.DEFAULT_BOLD
            paint.textAlign = Paint.Align.LEFT

            val titleBounds = Rect()
            paint.getTextBounds(title, 0, title.length, titleBounds)

            val y = rect.height() / 2 + titleBounds.height() / 2 - titleBounds.bottom
            canvas.drawText(title, rect.left + horizontalPadding, rect.top + y, paint)

            clickableRegion = rect
        }

        fun handle(event: MotionEvent, swipedRect: RectF) {
            clickableRegion?.let {
                if (swipedRect.contains(event.x, event.y) && it.contains(event.x, event.y)) {
                    clickListener.onClick()
                }
            }
        }
    }
    //endregion
}

private fun List<SwipeHelper.UnderlayButton>.intrinsicWidth(): Float {
    if (isEmpty()) return 0.0f
    return map { it.intrinsicWidth }.reduce { acc, fl -> acc + fl }
}