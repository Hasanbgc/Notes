package com.example.tempnavigation.helpers

import android.content.Context
import android.graphics.Canvas
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.tempnavigation.R
import com.example.tempnavigation.adapters.NoteViewAdapter
import kotlin.math.abs

class SwipeToDeleteCallback(private val context: Context, private val adapter: NoteViewAdapter):ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {

    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.delete_white_24)
    private val buttonWidth = deleteIcon?.intrinsicWidth ?:0
    private val buttonHeight = deleteIcon?.intrinsicHeight ?: 0
    private val buttonBackground = ContextCompat.getColor(context,R.color.colorChartRed)
    private val swipeThreshold = 0.7f
    private val swipeLimit = 1f / 6f
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        return false
    }

    override fun getSwipeThreshold(viewHolder: ViewHolder): Float {
        return swipeLimit
    }
    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        val pos = viewHolder.adapterPosition
        val itemView = viewHolder.itemView
        val translateX = abs(itemView.translationX)
        val maxSwipeDistance = itemView.width/8
        if(translateX >maxSwipeDistance){
            Log.d("home", "onSwiped: $translateX > ${maxSwipeDistance}")
            revealButton(viewHolder,pos)
        }else{
            hideButton(viewHolder,pos)
        }
        adapter.deleteNoteItem(pos)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemWidth = itemView.width.toFloat()

        val maxSwipeDistance = itemWidth * swipeLimit
        if(actionState ==ItemTouchHelper.ACTION_STATE_SWIPE){
            val limitDx = Math.min(dX, maxSwipeDistance.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, limitDx, dY, actionState, isCurrentlyActive)
        }else{
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }

    }
//        val itemView = viewHolder.itemView
//        val itemHeight = itemView.bottom - itemView.top
//        val itemWidth = itemView.right - itemView.left
//
//        val maxSwipeDistance = itemWidth / 8
//        val swipeLimit = maxSwipeDistance * swipeThreshold
//
//
//        val backgroundColor = ColorDrawable(buttonBackground)
//        backgroundColor.setBounds(
//            itemView.right + dX.toInt(),
//            itemView.top,
//            itemView.right,
//            itemView.bottom
//        )
//        backgroundColor.draw(c)
//        val deleteButtonTop = itemView.top + (itemHeight - buttonHeight!!)/2
//        val deleteButtonLeft = itemView.right - buttonWidth - maxSwipeDistance
//        val deleteButtonRight = itemView.right - (itemWidth / 8)
//        val deleteButtonBottom = deleteButtonTop + buttonHeight
//
//        deleteIcon?.setBounds(deleteButtonLeft,deleteButtonTop,deleteButtonRight,deleteButtonBottom)
//        deleteIcon?.draw(c)
//
//        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//    }

    private fun revealButton(viewHolder: ViewHolder, pos:Int){
        if(viewHolder.adapterPosition == pos){
        viewHolder.itemView.findViewById<AppCompatImageButton>(R.id.reveal_delete_button).visibility = View.VISIBLE}

    }
    private fun hideButton(viewHolder: ViewHolder, pos: Int){
        viewHolder.itemView.findViewById<AppCompatImageButton>(R.id.reveal_delete_button).visibility = View.GONE
        viewHolder.itemView.translationX = 0f
    }
}