package com.example.tp1cm2021.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tp1cm2021.R
import com.example.tp1cm2021.adapters.NoteAdapter
import com.example.tp1cm2021.entities.Note
import com.example.tp1cm2021.fragments.CreateNoteFragment
import com.example.tp1cm2021.viewModel.NoteViewModel

class NoteList : AppCompatActivity(), CreateNoteFragment.NoteCreateDialogListener {

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

    //open note creation dialog
    fun launchCreateDialog(view: View) {
        val createNoteFragment = CreateNoteFragment().show(supportFragmentManager, "CreateNoteFragment")
    }

    //open note details dialog
    fun openNoteDetails(view: View) {

    }

    //create note and dismiss dialog
    override fun onCreateNote(dialog: DialogFragment, note: Note) {
        viewModel.insertNote(note)
        dialog.dismiss()
    }

    //dismiss dialog
    override fun onCancelCreate(dialog: DialogFragment) {
        dialog.dismiss()
    }

}