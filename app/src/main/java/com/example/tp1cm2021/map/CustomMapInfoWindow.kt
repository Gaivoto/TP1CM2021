package com.example.tp1cm2021.map

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.tp1cm2021.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class CustomMapInfoWindow(context: Context) : GoogleMap.InfoWindowAdapter {

    var mContext = context
    var mWindow = (context as Activity).layoutInflater.inflate(R.layout.map_info_window, null)

    private fun renderWindowText(marker: Marker, view: View){

        val windowTitle = view.findViewById<TextView>(R.id.markerWindowTitle)
        val windowDescription = view.findViewById<TextView>(R.id.markerWindowDescription)
        val windowDate = view.findViewById<TextView>(R.id.markerWindowDate)
        val windowType = view.findViewById<TextView>(R.id.markerWindowType2)
        val windowImage = view.findViewById<ImageView>(R.id.markerWindowImage)

        //take the data passed in a string as the info window's snippet and display it on the window's fields
        val description: String = marker.snippet.substring(0, marker.snippet.indexOf("»"))
        var substring: String = marker.snippet.substring(marker.snippet.indexOf("»") + 1)
        val tipo: String = substring.substring(0, substring.indexOf("»"))
        substring = substring.substring(substring.indexOf("»") + 1)
        val lastModified: String = substring.substring(0, substring.indexOf("»"))
        substring = substring.substring(substring.indexOf("»") + 1)
        val status: String = substring.substring(0, substring.indexOf("»"))
        substring = substring.substring(substring.indexOf("»"))

        windowTitle.text = marker.title
        windowDescription.text = description
        windowType.text = tipo

        if(status == "Active") {
            windowDate.text = mContext.getString(R.string.createdIn) + " " + lastModified
        } else {
            windowDate.text = mContext.getString(R.string.lastModified) + " " + lastModified
        }

        //if the report has an image, load it using the picasso library
        val image: String = substring.substring(1)

        Picasso.get()
                .load("http://cmtp1.000webhostapp.com/slimCMTP1/uploads/" + image + ".png")
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(windowImage, object : Callback {
                    override fun onSuccess() {
                        if (marker.isInfoWindowShown) {
                            marker.hideInfoWindow()
                            marker.showInfoWindow()
                        }
                    }

                    override fun onError(e: Exception?) {
                        Toast.makeText(mContext, mContext.getString(R.string.failedGetImage), Toast.LENGTH_SHORT).show()
                    }
                })
    }

    override fun getInfoContents(marker: Marker): View {
        renderWindowText(marker, mWindow)
        return mWindow
    }

    override fun getInfoWindow(marker: Marker): View? {
        renderWindowText(marker, mWindow)
        return mWindow
    }
}