package com.example.tp1cm2021.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tp1cm2021.entities.Note

@Dao
interface NoteDao {

    @Query("SELECT * FROM note ORDER BY id DESC")
    fun getNotes(): LiveData<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note: Note)

    @Query("UPDATE note SET title = :title, description = :description WHERE id = :id")
    suspend fun updateNote(title: String, description: String, id: Int)

    @Query("DELETE FROM note WHERE id = :id")
    suspend fun deleteNote(id: Int)
}