package com.example.tp1cm2021.database

import androidx.lifecycle.LiveData
import com.example.tp1cm2021.dao.NoteDao
import com.example.tp1cm2021.entities.Note

class NoteRepository(private val noteDao: NoteDao) {

    val localNotes: LiveData<List<Note>> = noteDao.getNotes()

    suspend fun insertNote(note: Note){
        noteDao.insertNote(note)
    }

    suspend fun updateNote(title: String, description: String, id: Int){
        noteDao.updateNote(title, description, id)
    }

    suspend fun deleteNote(id: Int){
        noteDao.deleteNote( id)
    }
}