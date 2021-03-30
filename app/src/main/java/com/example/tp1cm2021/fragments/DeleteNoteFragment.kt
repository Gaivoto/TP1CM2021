package com.example.tp1cm2021.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.tp1cm2021.R
import com.example.tp1cm2021.entities.Note
import com.google.android.material.bottomnavigation.BottomNavigationView

class DeleteNoteFragment : DialogFragment() {

    lateinit var listener: NoteDeleteDialogListener

    //define interface to communicate with activity
    interface NoteDeleteDialogListener {
        fun onDeleteNote(dialog: DialogFragment, id: Int)
        fun onCancelDelete(dialog: DialogFragment)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            val dialogView = inflater.inflate(R.layout.delete_note_dialog, null)

            //display clicked note information on the dialog
            dialogView.findViewById<TextView>(R.id.idDelete).text = arguments!!["id"].toString()
            dialogView.findViewById<TextView>(R.id.textDelete).text = arguments!!["message"].toString() + " '" + arguments!!["title"].toString() + "'?"
            dialogView.findViewById<TextView>(R.id.bigTextDelete).text = arguments!!["dialogTitle"].toString()

            val deleteBtn = dialogView.findViewById<Button>(R.id.deleteBtnDelete)
            val cancelBtn = dialogView.findViewById<Button>(R.id.cancelBtnDelete)

            //add listeners to the dialog's custom button and make them communicate with the parent activity
            deleteBtn.setOnClickListener {

                val id: Int = dialogView.findViewById<TextView>(R.id.idDelete).text.toString().toInt()

                listener.onDeleteNote(this, id)
            }

            cancelBtn.setOnClickListener {
                listener.onCancelDelete(this)
            }

            builder.setView(dialogView)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    //attach parent as listener
    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as DeleteNoteFragment.NoteDeleteDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() + " must implement NoteDeleteDialogListener"))
        }
    }
}