package com.example.tp1cm2021.database

import androidx.lifecycle.LiveData
import com.example.tp1cm2021.dao.NoteDao
import com.example.tp1cm2021.entities.Note

class NoteRepository(private val noteDao: NoteDao) {

    //get all notes in the db
    val localNotes: LiveData<List<Note>> = noteDao.getNotes()

    //call dao method to insert note
    suspend fun insertNote(note: Note){
        noteDao.insertNote(note)
    }

    //call dao method to update note by id
    suspend fun updateNote(title: String, description: String, id: Int, date: String){
        noteDao.updateNote(title, description, id, date)
    }

    //call dao method to delete note by id
    suspend fun deleteNote(id: Int){
        noteDao.deleteNote(id)
    }
}