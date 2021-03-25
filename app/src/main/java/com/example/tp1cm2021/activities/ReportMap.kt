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

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.security.AccessController.getContext

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
    }

    //when the map loads
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney").icon(bitmapDescriptorFromVector(this@ReportMap, R.drawable.logout_bottom_nav)))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
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