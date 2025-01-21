package com.example.tempnavigation.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tempnavigation.R
import com.example.tempnavigation.models.NoteModel
import com.example.tempnavigation.viewmodels.AddNoteFragmentViewModel


class NoteViewAdapter(
    private val context: Context,
    private val viewModel: AddNoteFragmentViewModel): ListAdapter<NoteModel, NoteViewAdapter.NoteHolder>(noteDiffCallBack()) {

    var onItemClick: ((NoteModel) -> Unit)? = null
    var mNoteList: MutableList<NoteModel> = mutableListOf()
    private val TAG = "NoteViewAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.note_item_with_image, parent, false)
        return NoteHolder(itemView)
    }

    inner class NoteHolder(val note: View) : RecyclerView.ViewHolder(note) {

        fun bind(noteModel: NoteModel, position: Int, isSelectedItem: Boolean) {
            val textViewTitle: TextView = itemView.findViewById(R.id.mtextViewItemTitle)
            val textViewDescription: TextView = itemView.findViewById(R.id.mtextViewItemDescription)
            val locationIcon: ImageView = itemView.findViewById(R.id.location_icon)
            val imageView: ImageView = itemView.findViewById(R.id.image_view)
            val selectView: CheckBox? = itemView.findViewById(R.id.id_checkbox)
            val favouriteView:ImageView = itemView.findViewById(R.id.fav_icon)

            textViewTitle.text = noteModel.title
            textViewDescription.text = noteModel.description
            if (noteModel.locationLat == 0.0 && noteModel.locationLong == 0.0) {
                locationIcon.visibility = View.GONE
            } else {
                locationIcon.visibility = View.VISIBLE
            }
            if (noteModel.imageUri.isNotEmpty()) {
                imageView.visibility = View.VISIBLE
                Glide.with(context).load(noteModel.imageUri).into(imageView)
            }else{
                imageView.visibility = View.GONE
            }
            if(noteModel.favourite){
                Log.d(TAG,"favourite initial = ${noteModel.favourite}")
                favouriteView.setImageResource(R.drawable.favorite_24)
            }else{
                Log.d(TAG,"favourite initial as false = ${noteModel.favourite}")
                favouriteView.setImageResource(R.drawable.favorite_border_24)
            }

            if (longPressed){
                selectView?.visibility = View.VISIBLE
            }else{
                selectView?.visibility = View.GONE
            }

            selectView?.setOnClickListener {

                if (longPressed) {
                    if (isSelectedItem){
                        selectedItems.remove(position)
                    } else {
                        selectedItems.add(position)
                    }
                    notifyItemChanged(position)
                }
            }

            favouriteView.setOnClickListener{
                noteModel.favourite = !noteModel.favourite
                Log.d(TAG,"favourite ClickListener=  ${noteModel.favourite}")
                if(noteModel.favourite){
                    Log.d(TAG,"favourite ClickListener set true}")
                    favouriteView.setImageResource(R.drawable.favorite_24)
                }else{
                    Log.d(TAG,"favourite ClickListener set false}")
                    favouriteView.setImageResource(R.drawable.favorite_border_24)
                }
                Log.d(TAG,"note = $noteModel")
                viewModel.update(noteModel,{},{})
                notifyItemChanged(position)
            }
            note.setOnLongClickListener {
                longPressed = true
                selectedItems.clear()
                selectedItems.add(position)
                notifyItemChanged(position)
                true
            }
            itemView.setOnClickListener {
                if (longPressed) {
                    if (selectedItems.contains(position)) {
                        selectedItems.remove(position)
                    } else {
                        selectedItems.add(position)
                    }
                    notifyItemChanged(position)
                } else {
                    onItemClick?.invoke(
                        NoteModel(
                            noteModel.id,
                            noteModel.title,
                            noteModel.description,
                            noteModel.locationLat,
                            noteModel.locationLong,
                            noteModel.imageUri,
                            noteModel.alarmTime,
                            noteModel.savedTime,
                            noteModel.favourite,
                            noteModel.archive
                        )
                    )
                }
            }

            selectView?.isChecked = isSelectedItem

        }

    }



    override fun getItemViewType(position: Int): Int {
        return if (currentList[position].imageUri.isNotEmpty()) WITHIMAGE else WITHOUTIMAGE
    }

//    override fun getItemCount(): Int {
//        return mNoteList.size
//    }

    /*fun setNote(noteList: List<NoteModel>) {
        val functionName = Throwable().stackTrace[1].methodName
        Log.d(TAG,"$functionName")
        this.mNoteList = noteList as MutableList<NoteModel>
        Log.d(TAG,"$noteList")
        submitList(noteList)
    }*/

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        val currentNote = getItem(position)
        holder.bind(currentNote, position, selectedItems.contains(position))

    }

    fun deleteNoteItem(pos: Int) {
        //Log.d(TAG, "deleteNoteItem: $pos ${mNoteList[pos]}")

        //mNoteList.sortByDescending { it.id}
        currentList.removeAt(pos)

        //Log.d(TAG, "after deleted noteList:")
        /*mNoteList.forEach{i->
            Log.d(TAG, "note id: ${i.id}, note title: ${i.title}")
        }*/


        notifyItemRemoved(pos)
    }

    companion object {
        const val WITHIMAGE = 1
        const val WITHOUTIMAGE = 2
        var longPressed = false
        var selectedItems = mutableSetOf<Int>()
    }
    class noteDiffCallBack: DiffUtil.ItemCallback<NoteModel>(){
        override fun areItemsTheSame(
            oldItem: NoteModel,
            newItem: NoteModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: NoteModel,
            newItem: NoteModel
        ): Boolean {
            return oldItem == newItem
        }

    }
}