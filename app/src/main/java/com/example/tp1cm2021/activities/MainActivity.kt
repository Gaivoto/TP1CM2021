package com.example.tp1cm2021.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tp1cm2021.R
import com.example.tp1cm2021.api.Endpoints
import com.example.tp1cm2021.api.LoginOutput
import com.example.tp1cm2021.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get username and password values stored in the shared preferences
        val sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferenceFile), Context.MODE_PRIVATE)

        val username: String? = sharedPreferences.getString(getString(R.string.usernameP), "")
        val autoLogin: Boolean = sharedPreferences.getBoolean("autoLogin", false)

        //if the user previously chose to login automatically, do so now
        //else, clear the username from the shared preferences
        if(username != "" && autoLogin) {
            val intent = Intent(this@MainActivity, ReportMap::class.java)
            startActivity(intent)
            finish()
        } else {
            var sharedPreferences: SharedPreferences = getSharedPreferences(getString(R.string.preferenceFile), Context.MODE_PRIVATE)
            with (sharedPreferences.edit()) {
                putString(getString(R.string.usernameP), "")
                putBoolean("autoLogin", false)
                commit()
            }
        }
    }

    //move to note list activity
    fun moveToNotes(view: View) {
        val intent = Intent(this, NoteList::class.java).apply {
            putExtra("BOT_NAV_BAR", false)
        }
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
            //hash the password using the SHA-256 algorithm so the original password does not leave the device and only the hash is compared in the backend
            val md: MessageDigest = MessageDigest.getInstance("SHA-256")

            md.update(password.toByteArray(Charsets.UTF_8))
            val clone: MessageDigest = md.clone() as MessageDigest
            val digest: ByteArray = clone.digest()

            val hashedPassword = StringBuilder()

            digest.forEach { byte -> hashedPassword.append(String.format("%02X", byte)) }

            //if they are not empty make a login request to the api
            val request = ServiceBuilder.buildService(Endpoints::class.java)
            val call = request.login(username, hashedPassword.toString())

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
                                        putBoolean("autoLogin", true)
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