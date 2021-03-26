package com.example.tp1cm2021.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.tp1cm2021.R
import com.example.tp1cm2021.entities.Note
import com.google.android.material.bottomnavigation.BottomNavigationView

class NoteDetailsFragment() : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            val dialogView = inflater.inflate(R.layout.note_details_dialog, null)

            //display clicked note information on the dialog
            dialogView.findViewById<TextView>(R.id.noteDetailsTitleText).text = arguments!!["title"].toString()
            dialogView.findViewById<TextView>(R.id.noteDetailsDescriptionText).text = arguments!!["description"].toString()
            dialogView.findViewById<TextView>(R.id.noteDetailsDateText).text = getString(R.string.lastModified) + " " + arguments!!["date"].toString()

            builder.setView(dialogView)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}