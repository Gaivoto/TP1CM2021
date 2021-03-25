package com.example.tp1cm2021.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.tp1cm2021.R
import com.example.tp1cm2021.api.Endpoints
import com.example.tp1cm2021.api.Report
import com.example.tp1cm2021.api.ServiceBuilder
import com.example.tp1cm2021.map.CustomMapInfoWindow

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportMap : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        //call functions when clicking on items on the bottom navigation bar
        bottomNav.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.noteList->noteListBottomNav()
                R.id.logout->logoutBottomNav()
            }
            true
        }

        val request = ServiceBuilder.buildService(Endpoints::class.java)
        val call = request.getReports(0.01f, 0.01f, null, null)

        //make call to the api to get all the report
        call.enqueue(object : Callback<List<Report>> {
            override fun onResponse(call: Call<List<Report>>, response: Response<List<Report>>) {
                //if the call is successful, display all the reports on the map
                //if not, display a toast saying so
                if(response.isSuccessful){
                    for(report in response.body()!!) {
                        val bitmapDescriptor: BitmapDescriptor? = when (report.tipo) {
                            "Acidente" -> bitmapDescriptorFromVector(this@ReportMap, R.drawable.map_marker_accident)
                            "Obras" -> bitmapDescriptorFromVector(this@ReportMap, R.drawable.map_marker_construction)
                            else -> {
                                bitmapDescriptorFromVector(this@ReportMap, R.drawable.map_marker_other)
                            }
                        }

                        //format data in a string and pass it as the info window's snippet
                        var reportData: String = report.description + "»" + report.tipo + "»" + report.lastModified + "»" + report.status + "»"

                        if(report.image != null) {
                            reportData += report.image
                        }

                        mMap.addMarker(MarkerOptions()
                                .position(LatLng(report.lat.toDouble(), report.lon.toDouble()))
                                .title(report.title)
                                .icon(bitmapDescriptor)
                                .snippet(reportData))
                    }
                } else {
                    Toast.makeText(this@ReportMap, getString(R.string.failedGetReports), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Report>>, t: Throwable) {
                Toast.makeText(this@ReportMap, getString(R.string.failedGetReports), Toast.LENGTH_SHORT).show()
            }
        })
    }

    //when the map loads
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //change map style to dark mode
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_json));

        //change marker info window to my custom info window
        mMap.setInfoWindowAdapter(CustomMapInfoWindow(this))

        //move the camera
        val aaa = LatLng(11.0, 11.0)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(aaa))
    }

    //navigate to the note list activity
    private fun noteListBottomNav() {
        val intent = Intent(this, NoteList::class.java)
        startActivity(intent)
        finish()
    }

    //navigate to the login activity and clear shared preferences fields of username and password to prevent automatic login later
    private fun logoutBottomNav() {
        val sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferenceFile), Context.MODE_PRIVATE)
        with (sharedPreferences.edit()) {
            putString(getString(R.string.usernameP), "")
            putString(getString(R.string.passwordP), "")
            commit()
        }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    //function to convert a drawable into a bitmap to be used in the map as an icon for a marker
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}