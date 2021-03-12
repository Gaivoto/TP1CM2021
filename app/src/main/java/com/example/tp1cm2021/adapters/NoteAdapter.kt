package com.example.tp1cm2021.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tp1cm2021.R
import com.example.tp1cm2021.entities.Note

class NoteAdapter constructor(context: Context) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var notes = listOf<Note>()

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteId: TextView = itemView.findViewById(R.id.listItemId)
        val noteTitle: TextView = itemView.findViewById(R.id.noteTitle)
        val noteDescription: TextView = itemView.findViewById(R.id.noteDescription)
        val noteDate: TextView = itemView.findViewById(R.id.noteDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = inflater.inflate(R.layout.note_list_item, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val current = notes[position]

        holder.noteId.text = current.id.toString()
        holder.noteTitle.text = current.title
        holder.noteDescription.text = current.description
        holder.noteDate.text = current.date
    }

    //change data set of the adapter
    fun setNotes(notes: List<Note>) {
        this.notes = notes
        notifyDataSetChanged()
    }

    //get data set size
    override fun getItemCount(): Int {
        return notes.size
    }
}