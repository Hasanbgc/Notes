package com.example.tempnavigation.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.tempnavigation.R
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.utilities.FileUtil


class NoteViewAdapter(private val context: Context,private val onItemClick: (NoteModel) -> Unit) : RecyclerView.Adapter<NoteViewAdapter.NoteHolder>() {
    private var mNoteList: List<NoteModel> = listOf()

    class NoteHolder(val context: Context,itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val TAG = "NoteViewAdapter"
        private fun withImage(noteModel: NoteModel){
            val textViewTitle: TextView = itemView.findViewById(R.id.mtextViewItemTitle)
            val textViewDescription: TextView = itemView.findViewById(R.id.mtextViewItemDescription)
            val textViewPriority: TextView = itemView.findViewById(R.id.mtextViewPriority)
            val imageView:ImageView = itemView.findViewById(R.id.image_view)

            textViewTitle.text = noteModel.title
            textViewDescription.text = noteModel.description
            textViewPriority.text = noteModel.priority.toString()
            if (noteModel.imageUri.isNotEmpty()) {
                //val bitmap = FileUtil.getImageFromInternalStorage(context,noteModel.imageUri)
                Log.d(TAG,"NoteViewAdapter = ${noteModel.imageUri}")
                imageView.visibility = View.VISIBLE
                imageView.setImageURI(noteModel.imageUri.toUri())
            }

        }
        private fun withOutImage(noteModel: NoteModel){
            val textViewTitle: TextView = itemView.findViewById(R.id.textViewItemTitle)
            val textViewDescription: TextView = itemView.findViewById(R.id.textViewItemDescription)
            val textViewPriority: TextView = itemView.findViewById(R.id.textViewPriority)

            textViewTitle.text = noteModel.title
            textViewDescription.text = noteModel.description
            textViewPriority.text = noteModel.priority.toString()

        }




        fun bind(noteModel: NoteModel){
            when(noteModel.imageUri.isNotEmpty()){
                true -> withImage(noteModel)
                else -> withOutImage(noteModel)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        val itemView:View =  when(viewType){
            WITHIMAGE ->{ LayoutInflater.from(parent.context).inflate(R.layout.note_item_with_image, parent, false) }
            else -> {LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)}
        }
        return NoteHolder(context,itemView)
    }

    override fun getItemViewType(position: Int): Int {
        return if(mNoteList[position].imageUri.isNotEmpty()) WITHIMAGE else WITHOUTIMAGE
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
            onItemClick(NoteModel(currentNote.id,currentNote.title,currentNote.description,currentNote.priority,currentNote.imageUri))
        }
    }

    companion object{
        const val WITHIMAGE = 1
        const val WITHOUTIMAGE = 2
    }
}