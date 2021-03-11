package com.example.tp1cm2021.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tp1cm2021.R
import com.example.tp1cm2021.adapters.NoteAdapter
import com.example.tp1cm2021.entities.Note
import com.example.tp1cm2021.fragments.CreateNoteFragment
import com.example.tp1cm2021.fragments.EditNoteFragment
import com.example.tp1cm2021.fragments.NoteDetailsFragment
import com.example.tp1cm2021.viewModel.NoteViewModel

class NoteList : AppCompatActivity(), CreateNoteFragment.NoteCreateDialogListener, EditNoteFragment.NoteEditDialogListener {

    private lateinit var viewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_list)

        //set recycler adapter
        val noteRecycler = findViewById<RecyclerView>(R.id.noteListRecycler)
        val adapter = NoteAdapter(this)
        noteRecycler.adapter = adapter
        noteRecycler.layoutManager = LinearLayoutManager(this)

        //setup view model
        viewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        //observe note list on view model and auto update the recycler view when this changes
        viewModel.localNotes.observe(this, Observer { notes ->
            notes?.let { adapter.setNotes(it) }
        })
    }

    //open note details dialog
    fun openNoteDetails(view: View) {
        val noteDetailsFragment = NoteDetailsFragment()

        val args = Bundle()

        //add clicked note information to note dialog
        args.putString("title", view.findViewById<TextView>(R.id.noteTitle).text.toString())
        args.putString("description", view.findViewById<TextView>(R.id.noteDescription).text.toString())

        noteDetailsFragment.arguments = args
        noteDetailsFragment.show(supportFragmentManager, "NoteDetailsFragment")
    }

    //open note creation dialog
    fun launchCreateDialog(view: View) {
        val createNoteFragment = CreateNoteFragment().show(supportFragmentManager, "CreateNoteFragment")
    }

    //create note and dismiss note creation dialog
    override fun onCreateNote(dialog: DialogFragment, note: Note) {
        viewModel.insertNote(note)
        dialog.dismiss()
    }

    //dismiss note creation dialog
    override fun onCancelCreate(dialog: DialogFragment) {
        dialog.dismiss()
    }

    //open note edition dialog
    fun launchEditDialog(view: View) {
        val editNoteFragment = EditNoteFragment()

        val args = Bundle()

        val listItem = view.parent as ViewGroup

        //add clicked note information to note dialog
        args.putString("id", listItem.findViewById<TextView>(R.id.listItemId).text.toString())
        args.putString("title", listItem.findViewById<TextView>(R.id.noteTitle).text.toString())
        args.putString("description", listItem.findViewById<TextView>(R.id.noteDescription).text.toString())

        editNoteFragment.arguments = args
        editNoteFragment.show(supportFragmentManager, "EditNoteFragment")
    }

    //create note and dismiss note creation dialog
    override fun onEditNote(dialog: DialogFragment, note: Note) {
        viewModel.updateNote(note)
        dialog.dismiss()
    }

    //dismiss note creation dialog
    override fun onCancelEdit(dialog: DialogFragment) {
        dialog.dismiss()
    }

    //open note deletion dialog
    fun launchDeleteDialog(view: View) {

    }

}