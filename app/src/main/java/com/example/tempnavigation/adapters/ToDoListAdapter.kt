package com.example.tempnavigation.adapters

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.tempnavigation.R
import com.example.tempnavigation.databinding.ItemNoteBinding
import com.example.tempnavigation.databinding.ItemNoteEditBinding
import com.example.tempnavigation.models.ToDoItemModel
import com.example.tempnavigation.utilities.addToSecondLastPos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ToDoListAdapter(): ListAdapter<ToDoItemModel, ToDoListAdapter.ViewHolder>(ToDoListDiffCallBack()) {

    val todoList = mutableListOf<ToDoItemModel>()
    var toggle = false
    val onClickedItem:((pos:Int, data:ToDoItemModel)->Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
       return when(viewType){
            EDITABLE -> ViewHolder(ItemNoteEditBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            else -> ViewHolder(ItemNoteBinding.inflate(LayoutInflater.from(parent.context),parent,false))
       }
    }
    inner class ViewHolder(val itemView:ViewBinding ): RecyclerView.ViewHolder(itemView.root){
        fun bind(pos:Int,toDoItemModel: ToDoItemModel){
            var actionJob: Job? = null
            if (toDoItemModel.isEditable){
                when(itemView){
                    is ItemNoteBinding->{
                        Log.d("TAG","ItemNoteBinding")
                        Log.d("TAG1","${toDoItemModel.text.toString()}")
                        itemView.textViewNote.text = toDoItemModel.text.toString()
                        itemView.buttonDelete.setOnClickListener {
                            toggle = !toggle
                            if (toggle) {
                                itemView.buttonDelete.setImageResource(R.drawable.radio_button_checked_24)
                                //strike through the text if needed
                                itemView.textViewNote.paintFlags = itemView.textViewNote.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                actionJob = CoroutineScope(Dispatchers.Main).launch {
                                    delay(3000)
                                    itemView.buttonDelete.setImageResource(R.drawable.radio_button_unchecked_24)
                                    itemView.textViewNote.paintFlags = itemView.textViewNote.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                                    toggle = false
                                    currentList.removeAt(pos)
                                    notifyItemRemoved(pos)
                                    onClickedItem?.invoke(pos, toDoItemModel)

                                }
                            } else {
                                itemView.buttonDelete.setImageResource(R.drawable.radio_button_unchecked_24)
                                //strike through off the text if needed
                                itemView.textViewNote.paintFlags = itemView.textViewNote.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                                actionJob?.cancel()
                            }
                        }
                    }
                    is ItemNoteEditBinding->{
                        Log.d("TAG","ItemNoteEditBinding")
                        itemView.editTextMeetingPoints.setOnEditorActionListener { _, action, _ ->
                            if (action == EditorInfo.IME_ACTION_DONE) {
                                val text = itemView.editTextMeetingPoints.text
                                if(text?.isNotEmpty() == true) {
                                    Log.d("TAG","$text")
                                    val id = currentList.size
                                    val newDataList = currentList.toMutableList()
                                    val newData = ToDoItemModel(
                                        id,
                                        itemView.editTextMeetingPoints.text.toString(),
                                        false
                                    )
                                    newDataList.addToSecondLastPos(newData)
                                    Log.d("TAG", "dataListitem = ${newDataList.size}")
                                    // Toast.makeText(itemView.context, "Item Added =$id", Toast.LENGTH_SHORT).show()
                                    submitList(newDataList)
                                    itemView.editTextMeetingPoints.setText("")
                                }
                                true
                            } else {
                                false
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position,currentList[position])
    }

    override fun getItemViewType(position: Int): Int {
        return if(currentList[position].isEditable) EDITABLE else NOT_EDITABLE
    }

    class ToDoListDiffCallBack: DiffUtil.ItemCallback<ToDoItemModel>(){
        override fun areItemsTheSame(
            oldItem: ToDoItemModel,
            newItem: ToDoItemModel
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ToDoItemModel,
            newItem: ToDoItemModel
        ): Boolean {
            return oldItem == newItem
        }

    }
    companion object {
        val EDITABLE = 1
        val NOT_EDITABLE = 0
    }

}