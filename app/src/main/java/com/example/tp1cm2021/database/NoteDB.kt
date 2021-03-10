package com.example.tp1cm2021.database

import android.content.Context
import androidx.room.Database;
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tp1cm2021.dao.NoteDao
import com.example.tp1cm2021.entities.Note;
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.jvm.Volatile;

@Database(entities = arrayOf(Note::class), version = 4, exportSchema = false)
public abstract class NoteDB : RoomDatabase(){

    abstract fun noteDao(): NoteDao

    companion object {

        @Volatile
        private var INSTANCE: NoteDB? = null

        fun getDatabase(context: Context, scope: CoroutineScope): NoteDB {

            val temp = INSTANCE

            if(temp != null) {
                return temp
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDB::class.java,
                    "notes_database"
                ).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}