package com.example.tp1cm2021.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.example.tp1cm2021.R
import com.example.tp1cm2021.api.Endpoints
import com.example.tp1cm2021.api.LoginOutput
import com.example.tp1cm2021.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get username and password values stored in the shared preferences
        val sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferenceFile), Context.MODE_PRIVATE)

        val username: String? = sharedPreferences.getString(getString(R.string.usernameP), "")
        val password: String? = sharedPreferences.getString(getString(R.string.passwordP), "")

        //if those values are valid make a login request to the api
        if(username != null && password != null && username != "" && password != ""){
            val request = ServiceBuilder.buildService(Endpoints::class.java)
            val call = request.login(username!!, password!!)

            call.enqueue(object : Callback<LoginOutput> {
                //if the login request is successful, go to the map activity
                //if the login credentials are invalid show a toast
                override fun onResponse(call: Call<LoginOutput>, response: Response<LoginOutput>) {
                    if(response.isSuccessful){
                        if(response.body()!!.error != null) {
                            Toast.makeText(this@MainActivity, getString(R.string.failedLogin), Toast.LENGTH_SHORT).show()
                        } else {
                            val intent = Intent(this@MainActivity, ReportMap::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }

                override fun onFailure(call: Call<LoginOutput>, t: Throwable) {
                    Toast.makeText(this@MainActivity, getString(R.string.failedLogin), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    //move to note list activity
    fun moveToNotes(view: View) {
        val intent = Intent(this, NoteList::class.java)
        startActivity(intent)
    }

    fun login(view: View) {
        //get username and password inserted by the user
        val username: String = (view.parent as ViewGroup).findViewById<EditText>(R.id.editUsername).text.toString()
        val password: String = (view.parent as ViewGroup).findViewById<EditText>(R.id.editPassword).text.toString()

        //if any of them is empty show it on a toast
        if(username == ""){
            Toast.makeText(this@MainActivity, getString(R.string.noUsername), Toast.LENGTH_SHORT).show()
        } else if(password == ""){
            Toast.makeText(this@MainActivity, getString(R.string.noPassword), Toast.LENGTH_SHORT).show()
        } else {

            //if they are not empty make a login request to the api
            val request = ServiceBuilder.buildService(Endpoints::class.java)
            val call = request.login(username, password)

            call.enqueue(object : Callback<LoginOutput> {
                //if the login request is successful, go to the map activity
                //if the login credentials are invalid show a toast
                override fun onResponse(call: Call<LoginOutput>, response: Response<LoginOutput>) {
                    if(response.isSuccessful){
                        if(response.body()!!.error != null) {
                            Toast.makeText(this@MainActivity, getString(R.string.failedLogin), Toast.LENGTH_SHORT).show()
                        } else {
                            //if the login credentials are valid and if the keep me logged checkbox is checked, store those credentials in the stored preferences
                            //for future automatic logins
                            var sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferenceFile), Context.MODE_PRIVATE)
                                with (sharedPreferences.edit()) {
                                    putString(getString(R.string.usernameP), username)
                                    commit()
                                }
                            if((view.parent as ViewGroup).findViewById<CheckBox>(R.id.loggedCheck).isChecked){
                                sharedPreferences = getSharedPreferences(getString(R.string.preferenceFile), Context.MODE_PRIVATE)
                                    with (sharedPreferences.edit()) {
                                        putString(getString(R.string.passwordP), password)
                                        commit()
                                    }
                            }
                            val intent = Intent(this@MainActivity, ReportMap::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }

                override fun onFailure(call: Call<LoginOutput>, t: Throwable) {
                    Toast.makeText(this@MainActivity, getString(R.string.failedLogin), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}