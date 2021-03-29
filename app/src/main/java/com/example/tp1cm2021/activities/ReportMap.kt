package com.example.tp1cm2021.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tp1cm2021.R
import com.example.tp1cm2021.api.Endpoints
import com.example.tp1cm2021.api.Report
import com.example.tp1cm2021.api.ServiceBuilder
import com.example.tp1cm2021.map.CustomMapInfoWindow
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

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
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

        //get location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val request = ServiceBuilder.buildService(Endpoints::class.java)
        val call = request.getReports(0.01f, 0.01f, null, null)

        //make call to the api to get all the reports
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

        //get current location and center on it
        getCurrentLocation()
    }

    //function to get the user's current location
    private fun getCurrentLocation() {
        //if the app does not have permission to access the user's location, request it to the user
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)

            getCurrentLocation()
            return
        } else {
            mMap.isMyLocationEnabled = true

            //get current location
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if(location != null) {
                    lastLocation = location
                    //center map on the current location
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lastLocation.latitude, lastLocation.longitude), 0f))
                }
            }
        }
    }

    companion object {
        // add to implement last known location
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    //function called when the user clicks on the search icon
    fun filterReports(view: View) {

        //get user filter inputs
        val radius: String = (view.parent as ViewGroup).findViewById<EditText>(R.id.filterDistanceEdit).text.toString()
        val type: String = (view.parent as ViewGroup).findViewById<Spinner>(R.id.filterTypeEdit).selectedItem.toString()

        var radiusFinal: Int? = null
        var typeFinal: String? = null

        //if a distance was inserted check if it is grater than 0, if not say so in a toast
        if(radius != "") {
            if(radius.toInt() > 0) {
                radiusFinal = radius.toInt()
            } else {
                Toast.makeText(this@ReportMap, getString(R.string.negativeDistance), Toast.LENGTH_SHORT).show()
            }
        }

        //if a type different from "All" and the app is in english translate the report type to portuguese for the API to receive the parameter
        if(type != "All"){
            typeFinal = when(type) {
                "Accident" -> "Acidente"
                "Construction" -> "Obras"
                "Other" -> "Outro"
                else -> type
            }
        }

        //remove all the markers from the map
        mMap.clear()

        val request = ServiceBuilder.buildService(Endpoints::class.java)
        val call = request.getReports(lastLocation.latitude.toFloat(), lastLocation.longitude.toFloat(), radiusFinal, typeFinal)

        //make call to the api to get the filtered reports
        call.enqueue(object : Callback<List<Report>> {
            override fun onResponse(call: Call<List<Report>>, response: Response<List<Report>>) {
                //if the call is successful, display the filtered reports on the map
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

    //called when clicking the fab - update current location and go to the report creation activity
    fun goToReportCreation(view: View) {
        getCurrentLocation()

        val intent = Intent(this, CreateReport::class.java).apply {
            putExtra("LAT", lastLocation.latitude)
            putExtra("LON", lastLocation.longitude)
        }
        startActivity(intent)
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