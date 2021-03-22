package com.example.tp1cm2021.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import com.example.tp1cm2021.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    //move to note list activity
    fun moveToNotes(view: View) {
        val intent = Intent(this, NoteList::class.java)
        startActivity(intent)
    }

    fun login(view: View) {
        val intent = Intent(this, ReportMap::class.java)
        startActivity(intent)
    }
}