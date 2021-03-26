package com.example.tp1cm2021.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.example.tp1cm2021.R
import com.example.tp1cm2021.entities.Note
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EditNoteFragment : DialogFragment() {

    lateinit var listener: NoteEditDialogListener

    //define interface to communicate with activity
    interface NoteEditDialogListener {
        fun onEditNote(dialog: DialogFragment, note: Note)
        fun onCancelEdit(dialog: DialogFragment)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            val dialogView = inflater.inflate(R.layout.edit_note_dialog, null)

            //display clicked note information on the dialog
            dialogView.findViewById<TextView>(R.id.idEdit).text = arguments!!["id"].toString()
            dialogView.findViewById<TextView>(R.id.editTitleEdit).text = arguments!!["title"].toString()
            dialogView.findViewById<TextView>(R.id.editDescriptionEdit).text = arguments!!["description"].toString()
            dialogView.findViewById<TextView>(R.id.dateEdit).text = getString(R.string.lastModified) + " " + arguments!!["date"].toString()

            val editBtn = dialogView.findViewById<Button>(R.id.editBtn)
            val cancelBtn = dialogView.findViewById<Button>(R.id.cancelEditBtn)

            //add listeners to the dialog's custom button and make them communicate with the parent activity
            editBtn.setOnClickListener {

                val id: Int = dialogView.findViewById<TextView>(R.id.idEdit).text.toString().toInt()
                val title: String = dialogView.findViewById<EditText>(R.id.editTitleEdit).text.toString()
                val description: String = dialogView.findViewById<EditText>(R.id.editDescriptionEdit).text.toString()
                val date: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

                listener.onEditNote(this, Note(id, title, description, date))
            }

            cancelBtn.setOnClickListener {
                listener.onCancelEdit(this)
            }

            builder.setView(dialogView)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    //attach parent as listener
    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as EditNoteFragment.NoteEditDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + " must implement NoteEditDialogListener"))
        }
    }
}