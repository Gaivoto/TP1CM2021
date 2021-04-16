package com.example.tp1cm2021.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.tp1cm2021.R
import com.example.tp1cm2021.api.Endpoints
import com.example.tp1cm2021.api.PutBody
import com.example.tp1cm2021.api.ReportNonSelectOutput
import com.example.tp1cm2021.api.ServiceBuilder
import com.example.tp1cm2021.fragments.DeleteNoteFragment
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditDeleteReport : AppCompatActivity(), DeleteNoteFragment.NoteDeleteDialogListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_delete_report)

        //get all the views that will have their values changed
        val idHolder = findViewById<TextView>(R.id.editReportIdHolder)
        val titleView = findViewById<TextView>(R.id.reportDetailsTitle)
        val descriptionView = findViewById<TextView>(R.id.reportDetailsDescription)
        val typeView = findViewById<TextView>(R.id.reportDetailsType)
        val dateView = findViewById<TextView>(R.id.reportDetailsDate)
        val imageView = findViewById<ImageView>(R.id.reportDetailsImage)

        //get the title from the marker and set the title view text to it
        titleView.text = intent.getStringExtra("TITLE")

        //get the formatted details from the marker
        val details: String = intent.getStringExtra("DETAILS")

        //handle the formatted and separate them into variables
        val id: String = details.substring(0, details.indexOf("»"))
        var substring: String = details.substring(details.indexOf("»") + 1)
        val description: String = substring.substring(0, substring.indexOf("»"))
        substring = substring.substring(substring.indexOf("»") + 1)
        val tipo: String = substring.substring(0, substring.indexOf("»"))
        substring = substring.substring(substring.indexOf("»") + 1)
        val lastModified: String = substring.substring(0, substring.indexOf("»"))
        substring = substring.substring(substring.indexOf("»") + 1)
        val status: String = substring.substring(0, substring.indexOf("»"))
        substring = substring.substring(substring.indexOf("»") + 1)
        val username: String = substring.substring(0, substring.indexOf("»"))
        substring = substring.substring(substring.indexOf("»") + 1)
        val image: String = substring.substring(0)

        //assign those variables with details' values to the views
        idHolder.text = id
        descriptionView.text = description
        typeView.text = getString(R.string.typeDisplay) + " " + when (tipo) {
            "Acidente" -> getString(R.string.accident)
            "Obras" -> getString(R.string.construction)
            else -> getString(R.string.other)
        }

        if(status == "Active") {
            dateView.text = getString(R.string.createdIn) + " " + lastModified
        } else {
            dateView.text = getString(R.string.lastModified) + " " + lastModified
        }

        //request to load the image using the library Picasso
        Picasso.get()
            .load("http://10.0.2.2/slimCMTP1/uploads/" + image + ".png")
            .placeholder(R.drawable.marker_info_placeholder)
            .error(R.drawable.marker_info_placeholder)
            .into(imageView)

        //get logged user from the shared preferences and compare it to the creator of the report
        val sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferenceFile), Context.MODE_PRIVATE)

        val user: String? = sharedPreferences.getString(getString(R.string.usernameP), "")

        //if the currently logged in user is not the creator of the report hide the edit and delete buttons
        if(username != user) {
            findViewById<Button>(R.id.reportDetailsEditBtn).visibility = View.GONE
            findViewById<Button>(R.id.reportDetailsDeleteBtn).visibility = View.GONE
        }
    }

    //called when the edit button is clicked - enables editing of the report
    fun editBtnClicked(view: View) {
        //get text views and edit texts
        val titleView = findViewById<TextView>(R.id.reportDetailsTitle)
        val descriptionView = findViewById<TextView>(R.id.reportDetailsDescription)
        val titleEdit = findViewById<EditText>(R.id.reportDetailsTitleEdit)
        val descriptionEdit = findViewById<EditText>(R.id.reportDetailsDescriptionEdit)

        //change edits texts' texts to match the text views' text
        titleEdit.setText(titleView.text)
        descriptionEdit.setText(descriptionView.text)

        //hide text views and show edit texts
        titleEdit.visibility = View.VISIBLE
        descriptionEdit.visibility = View.VISIBLE
        titleView.visibility = View.INVISIBLE
        descriptionView.visibility = View.INVISIBLE

        //hide default buttons and show edition buttons
        findViewById<Button>(R.id.reportDetailsEditBtn).visibility = View.INVISIBLE
        findViewById<Button>(R.id.reportDetailsDeleteBtn).visibility = View.INVISIBLE
        findViewById<Button>(R.id.reportDetailsCloseBtn).visibility = View.INVISIBLE
        findViewById<Button>(R.id.reportDetailsCancelBtn).visibility = View.VISIBLE
        findViewById<Button>(R.id.reportDetailsConfirmBtn).visibility = View.VISIBLE
    }

    //called when the delete button is clicked - show the report deletion confirmation dialog
    fun deleteBtnClicked(view: View) {
        val deleteNoteFragment = DeleteNoteFragment()

        val args = Bundle()

        val listItem = view.parent as ViewGroup

        //add clicked note information to note dialog
        args.putString("id", findViewById<TextView>(R.id.editReportIdHolder).text.toString())
        args.putString("title", findViewById<TextView>(R.id.reportDetailsTitle).text.toString())
        args.putString("message", getString(R.string.sureDeleteReport))
        args.putString("dialogTitle", getString(R.string.deleteReport))

        deleteNoteFragment.arguments = args
        deleteNoteFragment.show(supportFragmentManager, "DeleteNoteFragment")
    }

    //called when report deletion is confirmed - make request to the API to delete the report and return to the report map activity
    override fun onDeleteNote(dialog: DialogFragment, id: Int) {
        //get currently logged in user's username from the shared preferences
        val sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferenceFile), Context.MODE_PRIVATE)

        val username: String? = sharedPreferences.getString(getString(R.string.usernameP), "")

        val request = ServiceBuilder.buildService(Endpoints::class.java)
        val call = request.deleteReport(id, username!!)

        //make a request to the API to delete the report
        call.enqueue(object : Callback<ReportNonSelectOutput> {
            //if the request is successful, display in a toast that the report was deleted and move to the report map activity
            //if not, say in a toast that an error occurred
            override fun onResponse(call: Call<ReportNonSelectOutput>, response: Response<ReportNonSelectOutput>) {
                if(response.isSuccessful && response.body()!!.error == null){
                    Toast.makeText(this@EditDeleteReport, getString(R.string.successDeletingReport), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@EditDeleteReport, ReportMap::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@EditDeleteReport, getString(R.string.errorDeletingReport), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ReportNonSelectOutput>, t: Throwable) {
                Toast.makeText(this@EditDeleteReport, getString(R.string.errorDeletingReport), Toast.LENGTH_SHORT).show()
            }
        })

        dialog.dismiss()
    }

    //called when report deletion is canceled - dismiss report deletion dialog
    override fun onCancelDelete(dialog: DialogFragment) {
        dialog.dismiss()
    }

    //called when the close button is clicked - go to the map activity
    fun closeBtnClicked(view: View) {
        val intent = Intent(this, ReportMap::class.java)
        //remove duplicate map activity from stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    //called when the confirm button is clicked - makes a request to the API to update the report with the user input values
    fun confirmBtnClicked(view: View) {

        //get user input values
        val title: String = findViewById<EditText>(R.id.reportDetailsTitleEdit).text.toString()
        val description: String = findViewById<EditText>(R.id.reportDetailsDescriptionEdit).text.toString()

        //verify that the user inputs are not empty
        //if they are, say so in a toast
        if(title == "") {
            Toast.makeText(this@EditDeleteReport, getString(R.string.noTitle), Toast.LENGTH_SHORT).show()
        } else if(description == "") {
            Toast.makeText(this@EditDeleteReport, getString(R.string.noDescription), Toast.LENGTH_SHORT).show()
        //if they are not, get the currently logged in user's username from the shared preferences and create a body for the request
        } else {
            val sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferenceFile), Context.MODE_PRIVATE)

            val username: String? = sharedPreferences.getString(getString(R.string.usernameP), "")

            val id: Int = findViewById<TextView>(R.id.editReportIdHolder).text.toString().toInt()

            val body = PutBody(title, description, username!!)

            val request = ServiceBuilder.buildService(Endpoints::class.java)
            val call = request.updateReport(id, body)

            //make the request to the API to update the report with the new values
            //say in a toast if the request was successful and if it was move to the report map
            call.enqueue(object : Callback<ReportNonSelectOutput> {
                override fun onResponse(call: Call<ReportNonSelectOutput>, response: Response<ReportNonSelectOutput>) {
                    if(response.isSuccessful && response.body()!!.error == null){
                        Toast.makeText(this@EditDeleteReport, getString(R.string.successUpdatingReport), Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@EditDeleteReport, ReportMap::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@EditDeleteReport, getString(R.string.errorUpdatingReport), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ReportNonSelectOutput>, t: Throwable) {
                    Toast.makeText(this@EditDeleteReport, getString(R.string.errorUpdatingReport), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    //called when the cancel button is clicked - cancels the editing of the report
    fun cancelBtnClicked(view: View) {
        //get text views and edit texts
        val titleView = findViewById<TextView>(R.id.reportDetailsTitle)
        val descriptionView = findViewById<TextView>(R.id.reportDetailsDescription)
        val titleEdit = findViewById<EditText>(R.id.reportDetailsTitleEdit)
        val descriptionEdit = findViewById<EditText>(R.id.reportDetailsDescriptionEdit)

        //clear edits texts' texts
        titleEdit.setText("")
        descriptionEdit.setText("")

        //hide text views and show edit texts
        titleEdit.visibility = View.INVISIBLE
        descriptionEdit.visibility = View.INVISIBLE
        titleView.visibility = View.VISIBLE
        descriptionView.visibility = View.VISIBLE

        //show default buttons and hide edition buttons
        findViewById<Button>(R.id.reportDetailsCancelBtn).visibility = View.INVISIBLE
        findViewById<Button>(R.id.reportDetailsConfirmBtn).visibility = View.INVISIBLE
        findViewById<Button>(R.id.reportDetailsEditBtn).visibility = View.VISIBLE
        findViewById<Button>(R.id.reportDetailsDeleteBtn).visibility = View.VISIBLE
        findViewById<Button>(R.id.reportDetailsCloseBtn).visibility = View.VISIBLE
    }
}