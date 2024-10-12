package com.example.tempnavigation.listener

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemLongPressListener(
    context: Context,
    private val recyclerView: RecyclerView,
    private val onItemLongPress: (position: Int) -> Unit
) : RecyclerView.SimpleOnItemTouchListener() {
    private val longPressGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {
            val childView = recyclerView.findChildViewUnder(e.x, e.y)
            if (childView != null) {
                val position = recyclerView.getChildAdapterPosition(childView)
                onItemLongPress(position)
            }
        }
    })

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        longPressGestureDetector.onTouchEvent(e)
        return false
    }
}