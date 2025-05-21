package com.kstraupenieks.asca

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.kstraupenieks.asca.R
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        // Check if user is already logged in
        if (sharedPreferences.getString("token", null) != null) {
            navigateToHomePage()
        }

        // Initialize Views
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // Navigate to Register Activity
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Handle Login Button Click
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty()) {
                etUsername.error = "Enter Username"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Enter Password"
                return@setOnClickListener
            }

            loginUser(username, password)
        }
    }

    private fun loginUser(username: String, password: String) {
        val url = Constants.BASE_URL + "login.php"

        val request = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                try {
                    println("DEBUG: Server Response: $response") // Log server response

                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.getBoolean("success")

                    if (success) {
                        val token = jsonResponse.getString("token")
                        val username = jsonResponse.getString("username")

                        // Save token and username in SharedPreferences
                        sharedPreferences.edit().apply {
                            putString("token", token)
                            putString("username", username)
                            apply()
                        }

                        Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                        navigateToHomePage()
                    } else {
                        val message = jsonResponse.optString("message", "Unknown error occurred")
                        println("DEBUG: Error Message: $message") // Log error
                        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    println("DEBUG: JSON Parsing Error: ${e.message}") // Log JSON error
                    Toast.makeText(this@LoginActivity, "Invalid server response", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                println("DEBUG: Volley Error: ${error.message}") // Log Volley error
                Toast.makeText(this@LoginActivity, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "username" to username,
                    "password" to password
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun navigateToHomePage() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
