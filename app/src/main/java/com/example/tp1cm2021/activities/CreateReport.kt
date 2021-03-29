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
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.tp1cm2021.R
import com.example.tp1cm2021.api.Endpoints
import com.example.tp1cm2021.api.Report
import com.example.tp1cm2021.api.ReportNonSelectOutput
import com.example.tp1cm2021.api.ServiceBuilder
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*

class CreateReport : AppCompatActivity() {
    private lateinit var location: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_report)

        location = LatLng(intent.getDoubleExtra("LAT", 0.0), intent.getDoubleExtra("LON", 0.0))
    }

    //called when upload from gallery image view is clicked - open gallery to choose an image
    fun getGalleryImage(view: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1)
    }

    //called when take photo image view is clicked - open camera to take a photo and puts photo in image view
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
                Toast.makeText(this@CreateReport, getString(R.string.photoError), Toast.LENGTH_SHORT).show()
            }
        }
    }

    //called when photo is taken with the camera inside the app
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //if the photo is taken and not cancelled by the user
        if (requestCode == 1 && resultCode == RESULT_OK) {

            //hide images to take photo or upload image
            findViewById<ImageView>(R.id.createReportGalleryImage).visibility = View.INVISIBLE
            findViewById<ImageView>(R.id.createReportPhotoImage).visibility = View.INVISIBLE

            //show image view that holds taken photo and set its image as the taken photo
            val image = findViewById<ImageView>(R.id.createReportImage)
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

    //called when the clear image text is called - hides taken photo/uploaded image and shows image insertion option image views again
    fun clearImage(view: View) {
        findViewById<ImageView>(R.id.createReportGalleryImage).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.createReportPhotoImage).visibility = View.VISIBLE
        findViewById<ImageView>(R.id.createReportImage).visibility = View.INVISIBLE
    }

    //called when the create report button is clicked - make post request to the API to create a report
    fun createClick(view: View) {

        //get the user input values
        val titleS: String = findViewById<TextView>(R.id.createReportTitleEdit).text.toString()
        val descriptionS: String = findViewById<TextView>(R.id.createReportDescriptionEdit).text.toString()

        //translate the type of report to portuguese for the API
        val typeS: String? = when (findViewById<Spinner>(R.id.createReportTypeSpinner).selectedItem.toString()) {
            "Accident" -> "Acidente"
            "Construction" -> "Obras"
            "Other" -> "Outro"
            else -> {
                findViewById<Spinner>(R.id.createReportTypeSpinner).selectedItem.toString()
            }
        }

        val sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferenceFile), Context.MODE_PRIVATE)

        val usernameS: String? = sharedPreferences.getString(getString(R.string.usernameP), "")

        //verify if there is a title, description and image
        //if there isn't, say so in a toast
        if(titleS == "") {
            Toast.makeText(this@CreateReport, getString(R.string.noTitle), Toast.LENGTH_SHORT).show()
        } else if(descriptionS == "") {
            Toast.makeText(this@CreateReport, getString(R.string.noDescription), Toast.LENGTH_SHORT).show()
        } else if(findViewById<ImageView>(R.id.createReportImage).visibility == View.INVISIBLE) {
            Toast.makeText(this@CreateReport, getString(R.string.noImage), Toast.LENGTH_SHORT).show()
        } else {
            //if there is, create objects for the form with all the report's information
            val title: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), titleS)
            val description: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), descriptionS)
            val type: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), typeS)
            val username: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), usernameS)
            val lat: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), location.latitude.toString())
            val lon: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), location.longitude.toString())

            //handle the image and prepare it to be sent in a form
            val imgBitmap: Bitmap = findViewById<ImageView>(R.id.createReportImage).drawable.toBitmap()
            val imgFile: File = convertBitmapToFile("file", imgBitmap)
            val imgFileRequest: RequestBody = RequestBody.create(MediaType.parse("image/*"), imgFile)
            val image: MultipartBody.Part = MultipartBody.Part.createFormData("uploadimg", imgFile.name, imgFileRequest)

            val request = ServiceBuilder.buildService(Endpoints::class.java)
            val call = request.createReport(title, description, lat, lon, username, type, image)

            //make the post request to the API to create a report
            //if the request is not successful, display a toast saying so
            call.enqueue(object : Callback<ReportNonSelectOutput> {
                override fun onResponse(call: Call<ReportNonSelectOutput>, response: Response<ReportNonSelectOutput>) {
                    //if the request is successful, say so in a toast and then go to the report map activity
                    if(response.isSuccessful){
                        Toast.makeText(this@CreateReport, getString(R.string.successCreatingReport), Toast.LENGTH_SHORT).show()
                        val intent2 = Intent(this@CreateReport, ReportMap::class.java)
                        startActivity(intent2)
                        finish()
                    } else {
                        Toast.makeText(this@CreateReport, getString(R.string.errorCreatingReport), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ReportNonSelectOutput>, t: Throwable) {
                    Toast.makeText(this@CreateReport, getString(R.string.errorCreatingReport), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    //called when the cancel button is clicked - returns to the map, destroying current activity
    fun cancelClick(view: View) {
        val intent = Intent(this, ReportMap::class.java)
        //remove duplicate map activity from stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    //conversion from a bitmap to a file
    private fun convertBitmapToFile(fileName: String, bitmap: Bitmap): File {
        //create a file to write bitmap data
        val file = File(this@CreateReport.cacheDir, fileName)
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