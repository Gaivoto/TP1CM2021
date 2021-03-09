package com.example.tp1cm2021

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast

class NoteList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_list)
        Toast.makeText(this, "nova act", Toast.LENGTH_SHORT).show()
    }
}