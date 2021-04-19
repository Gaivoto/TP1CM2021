package com.example.tp1cm2021.activities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.DialogFragment
import com.example.tp1cm2021.R
import com.example.tp1cm2021.api.Endpoints
import com.example.tp1cm2021.api.PutBody
import com.example.tp1cm2021.api.ReportNonSelectOutput
import com.example.tp1cm2021.api.ServiceBuilder
import com.example.tp1cm2021.fragments.DeleteNoteFragment
import com.squareup.picasso.Picasso
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

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
        val imageHolder = findViewById<ImageView>(R.id.imageHolder)

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

        Picasso.get()
                .load("http://10.0.2.2/slimCMTP1/uploads/" + image + ".png")
                .placeholder(R.drawable.marker_info_placeholder)
                .error(R.drawable.marker_info_placeholder)
                .into(imageHolder)

        //get logged user from the shared preferences and compare it to the creator of the report
        val sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferenceFile), Context.MODE_PRIVATE)

        val user: String? = sharedPreferences.getString(getString(R.string.usernameP), "")

        //if the currently logged in user is not the creator of the report hide the edit and delete buttons
        if(username != user) {
            findViewById<Button>(R.id.reportDetailsEditBtn).visibility = View.GONE
            findViewById<Button>(R.id.reportDetailsDeleteBtn).visibility = View.GONE
        }
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

    //called when the edit button is clicked - enables editing of the report
    fun editBtnClicked(view: View) {
        //get text views and edit texts
        val titleView = findViewById<TextView>(R.id.reportDetailsTitle)
        val descriptionView = findViewById<TextView>(R.id.reportDetailsDescription)
        val titleEdit = findViewById<EditText>(R.id.reportDetailsTitleEdit)
        val descriptionEdit = findViewById<EditText>(R.id.reportDetailsDescriptionEdit)
        val clearImgText = findViewById<TextView>(R.id.clearImgText)

        //change edits texts' texts to match the text views' text
        titleEdit.setText(titleView.text)
        descriptionEdit.setText(descriptionView.text)

        //hide text views and show edit texts
        titleEdit.visibility = View.VISIBLE
        descriptionEdit.visibility = View.VISIBLE
        titleView.visibility = View.INVISIBLE
        descriptionView.visibility = View.INVISIBLE
        clearImgText.visibility = View.VISIBLE

        //hide default buttons and show edition buttons
        findViewById<Button>(R.id.reportDetailsEditBtn).visibility = View.INVISIBLE
        findViewById<Button>(R.id.reportDetailsDeleteBtn).visibility = View.INVISIBLE
        findViewById<Button>(R.id.reportDetailsCloseBtn).visibility = View.INVISIBLE
        findViewById<Button>(R.id.reportDetailsCancelBtn).visibility = View.VISIBLE
        findViewById<Button>(R.id.reportDetailsConfirmBtn).visibility = View.VISIBLE
    }

    //called when the confirm button is clicked - makes a request to the API to update the report with the user input values
    fun confirmBtnClicked(view: View) {

        //get user input values
        val titleS: String = findViewById<EditText>(R.id.reportDetailsTitleEdit).text.toString()
        val descriptionS: String = findViewById<EditText>(R.id.reportDetailsDescriptionEdit).text.toString()

        //verify that the user inputs are not empty
        //if they are, say so in a toast
        if(titleS == "") {
            Toast.makeText(this@EditDeleteReport, getString(R.string.noTitle), Toast.LENGTH_SHORT).show()
        } else if(descriptionS == "") {
            Toast.makeText(this@EditDeleteReport, getString(R.string.noDescription), Toast.LENGTH_SHORT).show()
        } else if(findViewById<ImageView>(R.id.reportDetailsImage).visibility != View.VISIBLE) {
            Toast.makeText(this@EditDeleteReport, getString(R.string.noImage), Toast.LENGTH_SHORT).show()
        //if they are not, get the currently logged in user's username from the shared preferences and create a body for the request
        } else {
            val sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferenceFile), Context.MODE_PRIVATE)
            val usernameS: String? = sharedPreferences.getString(getString(R.string.usernameP), "")

            val id: Int = findViewById<TextView>(R.id.editReportIdHolder).text.toString().toInt()

            val title: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), titleS)
            val description: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), descriptionS)
            val username: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), usernameS)

            //handle the image and prepare it to be sent in a form
            val imgBitmap: Bitmap = findViewById<ImageView>(R.id.reportDetailsImage).drawable.toBitmap()
            val imgFile: File = convertBitmapToFile("file", imgBitmap)
            val imgFileRequest: RequestBody = RequestBody.create(MediaType.parse("image/*"), imgFile)
            val image: MultipartBody.Part = MultipartBody.Part.createFormData("uploadimg", imgFile.name, imgFileRequest)

            val request = ServiceBuilder.buildService(Endpoints::class.java)
            val call = request.updateReport(id, title, description, username, image)

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

    //called when the clear image text is clicked
    fun clearImg(view: View) {

        val imageView = findViewById<ImageView>(R.id.reportDetailsImage)
        val clearImgText = findViewById<TextView>(R.id.clearImgText)
        val restoreImgText = findViewById<TextView>(R.id.restoreImgText)
        val uploadGalleryImage = findViewById<ImageView>(R.id.updateUploadGallery)
        val takePhotoImage = findViewById<ImageView>(R.id.updateTakePhoto)

        imageView.visibility = View.INVISIBLE
        clearImgText.visibility = View.INVISIBLE
        restoreImgText.visibility = View.VISIBLE
        uploadGalleryImage.visibility = View.VISIBLE
        takePhotoImage.visibility = View.VISIBLE
    }

    //called when the restore image text is clicked
    fun restoreImg(view: View) {

        val imageView = findViewById<ImageView>(R.id.reportDetailsImage)
        val imageHolder = findViewById<ImageView>(R.id.imageHolder)
        val clearImgText = findViewById<TextView>(R.id.clearImgText)
        val restoreImgText = findViewById<TextView>(R.id.restoreImgText)
        val galleryImg = findViewById<ImageView>(R.id.updateUploadGallery)
        val photoImg = findViewById<ImageView>(R.id.updateTakePhoto)

        imageView.setImageDrawable(imageHolder.drawable)
        imageView.visibility = View.VISIBLE
        clearImgText.visibility = View.VISIBLE
        restoreImgText.visibility = View.INVISIBLE
        galleryImg.visibility = View.INVISIBLE
        photoImg.visibility = View.INVISIBLE
    }

    //called when the upload from gallery image view is clicked
    fun uploadFromGallery(view: View) {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    //called when the take photo image view is clicked
    fun takePhoto(view: View) {

        //checks if the app has camera permission and if not asks for it to the user
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1)
            return
        } else {
            //if there is permission open camera and take photo
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            try {
                startActivityForResult(takePictureIntent, 1)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this@EditDeleteReport, getString(R.string.photoError), Toast.LENGTH_SHORT).show()
            }
        }
    }

    //called when photo is taken with the camera inside the app
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //if the photo is taken and not cancelled by the user
        if (requestCode == 1 && resultCode == RESULT_OK) {

            //hide images to take photo or upload image
            findViewById<ImageView>(R.id.updateUploadGallery).visibility = View.INVISIBLE
            findViewById<ImageView>(R.id.updateTakePhoto).visibility = View.INVISIBLE

            findViewById<TextView>(R.id.clearImgText).visibility = View.VISIBLE
            findViewById<TextView>(R.id.restoreImgText).visibility = View.VISIBLE

            //show image view that holds taken photo and set its image as the taken photo
            val image = findViewById<ImageView>(R.id.reportDetailsImage)
            image.visibility = View.VISIBLE

            //if the image came from the camera
            if(data!!.extras != null){
                val imageBitmap = data!!.extras!!.get("data") as Bitmap
                image.setImageBitmap(imageBitmap)
            } else {
                //if the image came from the gallery
                image.setImageURI(data?.data)
            }
        }
    }

    //called when the cancel button is clicked - cancels the editing of the report
    fun cancelBtnClicked(view: View) {
        //get text views and edit texts
        val titleView = findViewById<TextView>(R.id.reportDetailsTitle)
        val descriptionView = findViewById<TextView>(R.id.reportDetailsDescription)
        val titleEdit = findViewById<EditText>(R.id.reportDetailsTitleEdit)
        val descriptionEdit = findViewById<EditText>(R.id.reportDetailsDescriptionEdit)
        val clearImgText = findViewById<TextView>(R.id.clearImgText)
        val restoreImgText = findViewById<TextView>(R.id.restoreImgText)
        val imageView = findViewById<ImageView>(R.id.reportDetailsImage)
        val imageHolder = findViewById<ImageView>(R.id.imageHolder)
        val photoImg = findViewById<ImageView>(R.id.updateTakePhoto)
        val galleryImg = findViewById<ImageView>(R.id.updateUploadGallery)


        //clear edits texts' texts
        titleEdit.setText("")
        descriptionEdit.setText("")

        //hide text views and show edit texts
        titleEdit.visibility = View.INVISIBLE
        descriptionEdit.visibility = View.INVISIBLE
        clearImgText.visibility = View.INVISIBLE
        restoreImgText.visibility = View.INVISIBLE
        photoImg.visibility = View.INVISIBLE
        galleryImg.visibility = View.INVISIBLE
        titleView.visibility = View.VISIBLE
        descriptionView.visibility = View.VISIBLE

        imageView.setImageDrawable(imageHolder.drawable)
        imageView.visibility = View.VISIBLE

        //show default buttons and hide edition buttons
        findViewById<Button>(R.id.reportDetailsCancelBtn).visibility = View.INVISIBLE
        findViewById<Button>(R.id.reportDetailsConfirmBtn).visibility = View.INVISIBLE
        findViewById<Button>(R.id.reportDetailsEditBtn).visibility = View.VISIBLE
        findViewById<Button>(R.id.reportDetailsDeleteBtn).visibility = View.VISIBLE
        findViewById<Button>(R.id.reportDetailsCloseBtn).visibility = View.VISIBLE
    }

    //called when the close button is clicked - go to the map activity
    fun closeBtnClicked(view: View) {
        val intent = Intent(this, ReportMap::class.java)
        //remove duplicate map activity from stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    //conversion from a bitmap to a file
    private fun convertBitmapToFile(fileName: String, bitmap: Bitmap): File {
        //create a file to write bitmap data
        val file = File(this@EditDeleteReport.cacheDir, fileName)
        file.createNewFile()

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, bos)
        val bitMapData = bos.toByteArray()

        //write the bytes in file
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        try {
            fos?.write(bitMapData)
            fos?.flush()
            fos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }
}