package com.kstraupenieks.asca

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject



class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Views
        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhoneNumber = findViewById<EditText>(R.id.etPhoneNumber)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        // Navigate Back to Login
        tvBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Closes RegisterActivity
        }

        // Handle Register Button Click
        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phoneNumber = etPhoneNumber.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (fullName.isEmpty()) {
                etFullName.error = "Enter Full Name"
                return@setOnClickListener
            }
            if (username.isEmpty()) {
                etUsername.error = "Enter Username"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                etEmail.error = "Enter Email"
                return@setOnClickListener
            }
            if (phoneNumber.isEmpty()) {
                etPhoneNumber.error = "Enter Phone Number"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Enter Password"
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                etConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            registerUser(fullName, username, email, phoneNumber, password)
        }
    }

    private fun registerUser(fullName: String, username: String, email: String, phoneNumber: String, password: String) {
        val url = Constants.BASE_URL + "register.php"

        val request = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.getBoolean("success")
                    val message = jsonResponse.getString("message")

                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                    if (success) {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "full_name" to fullName,
                    "username" to username,
                    "email" to email,
                    "phone_number" to phoneNumber,
                    "password" to password
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }


}
