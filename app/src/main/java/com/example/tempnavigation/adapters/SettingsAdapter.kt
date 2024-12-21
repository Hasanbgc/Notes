package com.example.tempnavigation.adapters

import android.graphics.Paint
import android.os.Build
import android.util.Log
import com.example.tempnavigation.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.tempnavigation.models.ToDoItemModel
import com.google.android.material.radiobutton.MaterialRadioButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.sortByDescending


class SettingsAdapter(private val onClickedItem: (pos: Int, toDoItemModel: ToDoItemModel) -> Unit) :
    RecyclerView.Adapter<SettingsAdapter.settingHolder>() {
    private var dataListitem = mutableListOf<ToDoItemModel>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): settingHolder {
        when (viewType) {
            EDITABLE -> {
                Log.d("TAG", "onCreateViewHolder: item_note_edit")
                return settingHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_note_edit, parent, false)
                )

            }

            else -> {
                Log.d("TAG", "onCreateViewHolder: item_note")
                return settingHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
                )

            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onBindViewHolder(
        holder: settingHolder,
        position: Int
    ) {
        holder.bind(dataListitem[position])
    }

    override fun getItemCount(): Int {
        return dataListitem.size
    }

    override fun getItemViewType(position: Int): Int {
        Log.d("TAG", "pos: $position")
        return if (dataListitem[position].isEditable) EDITABLE else NOT_EDITABLE
    }

    inner class settingHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(toDoItemModel: ToDoItemModel) {
            var toggle = false
            Log.d("TAG", "model: $toDoItemModel")
            val editText = itemView.findViewById<TextView>(R.id.editTextMeetingPoints)
            var actionJob: Job? = null
            when (toDoItemModel.isEditable) {
                true -> {
                    editText.setOnEditorActionListener { _, action, _ ->
                        if (action.equals(EditorInfo.IME_ACTION_DONE)) {
                            val text = editText.text.trim().toString()
                            val id = dataListitem.size
                            val newDataList = dataListitem.toMutableList()
                            val newData = ToDoItemModel(id, text, false)
                            newDataList.addToSecondLastPos(newData)
                            Log.d("TAG", "dataListitem = ${newDataList.size}")
                            editText.text = ""
                            // Toast.makeText(itemView.context, "Item Added =$id", Toast.LENGTH_SHORT).show()
                            submitData(adapterPosition, newDataList)
                            editText.requestFocus()
                            true
                        } else {
                            false
                        }
                    }

                }

                else -> {
                    val textView = itemView.findViewById<TextView>(R.id.textViewNote)
                    val closeButton = itemView.findViewById<ImageView>(R.id.buttonDelete)
                    textView.text = "${toDoItemModel.text}"
                    toggle = false
                    closeButton.setOnClickListener {
                        toggle = !toggle
                        if (toggle) {
                            closeButton.setImageResource(R.drawable.radio_button_checked_24)
                            //strike through the text if needed
                            //textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                            actionJob = CoroutineScope(Dispatchers.Main).launch {
                                delay(3000)
                                closeButton.setImageResource(R.drawable.radio_button_unchecked_24)
                                textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                                dataListitem.removeAt(adapterPosition)
                                notifyItemRemoved(adapterPosition)
                                onClickedItem(adapterPosition, toDoItemModel)

                            }
                        } else {
                            closeButton.setImageResource(R.drawable.radio_button_unchecked_24)
                            //strike through off the text if needed
                            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                            actionJob?.cancel()
                        }
                    }

                }
            }
        }
    }

    fun submitData(pos: Int = 0, data: List<ToDoItemModel>) {
        dataListitem.apply {
            clear()
            addAll(data)
        }
        notifyItemInserted(pos)
    }

    companion object {
        const val EDITABLE = 1
        const val NOT_EDITABLE = 0

    }


    fun <T> MutableList<T>.addToSecondLastPos(data: T) {

        val lastItem = this.removeAt(this.size - 1)
        // Log.d("TAG", "lastValue = $lastItem")
        // val newList = this.toMutableList()
        // Log.d("TAG", "NewList = ${newList.size}")
        if (this.isNotEmpty()) {
            // Log.d("TAG", "this.size is > 1")
            this.add(this.size, data)
            this.add(this.size, lastItem)
        } else {
            // Log.d("TAG", "this.size is 0")
            this.add(0, data)
            this.add(1, lastItem)

        }

    }

}
//unused code
/* inner class DiffCallback(val newList: List<ToDoItemModel>) : DiffUtil.Callback() {
       override fun getOldListSize() = dataListitem.size
       override fun getNewListSize() = newList.size
       override fun areItemsTheSame(
           oldPos: Int,
           newPos: Int
       ): Boolean {
           return newList[newPos].id == dataListitem[oldPos].id
       }

       override fun areContentsTheSame(
           oldPos: Int,
           newPos: Int
       ): Boolean {
           if (oldPos >= 0 && oldPos < dataListitem.size && newPos < newList.size) {
               return newList[newPos] == dataListitem[oldPos]
           }
           return true
       }
   }*/