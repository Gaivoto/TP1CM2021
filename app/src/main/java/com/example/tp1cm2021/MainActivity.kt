package com.example.tp1cm2021

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun moveToNotes(view: View) {
        Log.i("EEE","aaaa")
        val intent = Intent(this, NoteList::class.java)
        startActivity(intent)
    }
}