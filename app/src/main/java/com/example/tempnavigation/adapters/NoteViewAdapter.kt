package com.example.tempnavigation.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tempnavigation.R
import com.example.tempnavigation.models.NoteModel


class NoteViewAdapter(private val onItemClick: (NoteModel) -> Unit) : RecyclerView.Adapter<NoteViewAdapter.NoteHolder>() {
    private var mNoteList: List<NoteModel> = listOf()

    class NoteHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textViewTitle: TextView = itemView.findViewById(R.id.textViewItemTitle)
        private val textViewDescription: TextView = itemView.findViewById(R.id.textViewItemDescription)
        private val textViewPriority: TextView = itemView.findViewById(R.id.textViewPriority)

        fun bind(noteModel: NoteModel){
            textViewTitle.text = noteModel.title
            textViewDescription.text = noteModel.description
            textViewPriority.text = noteModel.priority.toString()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        val itemView: View =
            LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteHolder(itemView)
    }

    override fun getItemCount(): Int {

        return mNoteList.size
    }

    fun setNote(noteList: List<NoteModel>) {
        this.mNoteList = noteList
        notifyDataSetChanged()
        Log.d("Adapter", "$mNoteList")
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        val currentNote = mNoteList[position]
        Log.d("Adapter", "$currentNote")
        holder.bind(currentNote)

        holder.itemView.setOnClickListener{
            onItemClick(NoteModel(currentNote.id,currentNote.title,currentNote.description,currentNote.priority))
        }
    }
}