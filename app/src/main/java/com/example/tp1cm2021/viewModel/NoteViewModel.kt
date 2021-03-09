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

    val localNotes: LiveData<List<Note>>

    init {
        val notesDao = NoteDB.getDatabase(application).noteDao()
        repository = NoteRepository(notesDao)
        localNotes = repository.localNotes
    }

    fun insertNote(note: Note) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertNote(note)
    }

    fun updateNote(title: String, description: String, id: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateNote(title, description, id)
    }

    fun deleteNote(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteNote(id)
    }
}