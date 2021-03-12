package com.example.tp1cm2021.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tp1cm2021.entities.Note

@Dao
interface NoteDao {

    //get all notes ordered by most recent to oldest
    @Query("SELECT * FROM note ORDER BY id DESC")
    fun getNotes(): LiveData<List<Note>>

    //insert note
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note: Note)

    //update note by id
    @Query("UPDATE note SET title = :title, description = :description, date = :date WHERE id = :id")
    suspend fun updateNote(title: String, description: String, id: Int, date: String)

    //delete note by id
    @Query("DELETE FROM note WHERE id = :id")
    suspend fun deleteNote(id: Int)

    //delete all notes
    @Query("DELETE FROM note")
    suspend fun deleteAll()
}