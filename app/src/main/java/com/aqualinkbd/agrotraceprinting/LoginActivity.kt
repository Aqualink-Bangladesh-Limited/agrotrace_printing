package com.aqualinkbd.agrotraceprinting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.aqualinkbd.agrotraceprinting.Models.LoginBaseResponse
import com.charityright.bd.Utils.CustomSharedPref
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

class LoginActivity : AppCompatActivity() {



    private lateinit var phone : String
    private lateinit var pass : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        CustomSharedPref.init(this)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val phoneField = findViewById<EditText>(R.id.phoneEditText)
        val passField = findViewById<EditText>(R.id.passwordEditText)

        loginButton.setOnClickListener {
            phone = phoneField.text.toString()
            pass = passField.text.toString()

            if (phone != "" && pass != ""){
                loginApiCall(phone,pass)
            }else{
                Toast.makeText(this,"Email & Password Files Can't Be Empty",Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun loginApiCall(email: String, pass: String) {

        val apiUrl = "http://agrotraces.com/api/v2/tobacco/login"

        val requestBody = FormBody.Builder()
            .add("phone", email)
            .add("password", pass)
            .build()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(apiUrl).post(requestBody)
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                println("Response Body$responseBody")

                if (response.isSuccessful && responseBody != null) {

                    val gson = Gson()
                    val apiResponse = gson.fromJson(responseBody, LoginBaseResponse::class.java)

                   println(apiResponse.data?.token)

                    // Update UI on the main thread
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity,"Login Successful",Toast.LENGTH_SHORT).show()
                        CustomSharedPref.write("Token",apiResponse.data?.token.toString())
                        println(apiResponse.data?.token)
                        startActivity(
                            Intent(
                                this@LoginActivity,
                                MainActivity::class.java
                            )
                        )
                        finish()
                    }

                } else {
                    Toast.makeText(this@LoginActivity,"Login Failed",Toast.LENGTH_SHORT).show()
                    //apiResponseTextView.text = "Error: ${response.code}"
                }

            } catch (e: IOException) {

                // Handle network errors
                withContext(Dispatchers.Main) {
                    //apiResponseTextView.text = "Network Error: ${e.message}"
                }
            }
        }
    }

}