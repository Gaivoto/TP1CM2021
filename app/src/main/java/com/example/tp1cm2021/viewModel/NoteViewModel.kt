package com.example.tp1cm2021.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.tp1cm2021.database.NoteDB
import com.example.tp1cm2021.database.NoteRepository
import com.example.tp1cm2021.entities.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application: Application): AndroidViewModel(application) {

    private val repository: NoteRepository

    var localNotes: LiveData<List<Note>>

    //get all notes from the repository
    init {
        val notesDao = NoteDB.getDatabase(application, viewModelScope).noteDao()
        repository = NoteRepository(notesDao)
        localNotes = repository.localNotes
    }

    //call repository method to insert note
    fun insertNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertNote(note)
    }

    //call repository method to update note by id
    fun updateNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateNote(note.title, note.description, note.id!!)
    }

    //call repository method to delete note by id
    fun deleteNote(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteNote(id)
    }
}