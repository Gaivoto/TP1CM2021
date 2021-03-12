package com.example.tp1cm2021.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.example.tp1cm2021.R
import com.example.tp1cm2021.entities.Note
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CreateNoteFragment : DialogFragment() {

    lateinit var listener: NoteCreateDialogListener

    //define interface to communicate with activity
    interface NoteCreateDialogListener {
        fun onCreateNote(dialog: DialogFragment, note: Note)
        fun onCancelCreate(dialog: DialogFragment)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            val dialogView = inflater.inflate(R.layout.create_note_dialog, null)

            val createBtn = dialogView.findViewById<Button>(R.id.createBtn)
            val cancelBtn = dialogView.findViewById<Button>(R.id.cancelBtn)

            //add listeners to the dialog's custom button and make them communicate with the parent activity
            createBtn.setOnClickListener {

                val title: String = dialogView.findViewById<EditText>(R.id.editTitle).text.toString()
                val description: String = dialogView.findViewById<EditText>(R.id.editDescription).text.toString()
                val date: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

                listener.onCreateNote(this, Note(null, title, description, date))
            }

            cancelBtn.setOnClickListener {
                listener.onCancelCreate(this)
            }

            builder.setView(dialogView)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    //attach parent as listener
    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as NoteCreateDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + " must implement NoteCreateDialogListener"))
        }
    }
}