package com.kstraupenieks.asca

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ProfileFragment : Fragment() {

    private lateinit var tvFullName: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhoneNumber: TextView
    private lateinit var btnChangePassword: Button
    private lateinit var btnChangePersonalInfo: Button
    private var personalInfoDialog: AlertDialog? = null


    private val client = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        tvFullName = view.findViewById(R.id.tvFullName)
        tvUsername = view.findViewById(R.id.tvUsername)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        btnChangePersonalInfo = view.findViewById(R.id.btnChangePersonalInfo)
        loadUserInfo()
        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
        btnChangePersonalInfo.setOnClickListener {
            showChangePersonalInfoDialog()
        }

        return view
    }

    private fun loadUserInfo() {
        val token = getToken() ?: return

        val requestBody = FormBody.Builder()
            .add("token", token)
            .build()

        val request = Request.Builder()
            .url(Constants.BASE_URL + "get_user_info.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                val json = JSONObject(result ?: "")
                if (json.getString("status") == "success") {
                    val fullName = json.getString("full_name")
                    val username = json.getString("username")
                    val email = json.getString("email")
                    val phone = json.optString("phone_number", "N/A")

                    requireActivity().runOnUiThread {
                        tvFullName.text = fullName
                        tvUsername.text = username
                        tvEmail.text = email
                        tvPhoneNumber.text = phone
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Invalid token", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)

        val currentPassword = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val newPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val confirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)
        val submitButton = dialogView.findViewById<Button>(R.id.btnSubmitPasswordChange)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .create()

        submitButton.setOnClickListener {
            val current = currentPassword.text.toString()
            val new = newPassword.text.toString()
            val confirm = confirmPassword.text.toString()

            if (new != confirm) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            val token = getToken() ?: return@setOnClickListener

            val requestBody = FormBody.Builder()
                .add("token", token)
                .add("current_password", current)
                .add("new_password", new)
                .build()

            val request = Request.Builder()
                .url(Constants.BASE_URL +"change_password.php")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val result = response.body?.string()
                    val json = JSONObject(result ?: "")
                    requireActivity().runOnUiThread {
                        if (json.getBoolean("success")) {
                            Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        } else {
                            Toast.makeText(requireContext(), json.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }

        dialog.show()
    }

    private fun showChangePersonalInfoDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_personal_info, null)

        val etFullName = dialogView.findViewById<EditText>(R.id.etFullName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val etPhoneNumber = dialogView.findViewById<EditText>(R.id.etPhoneNumber)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmitPersonalInfo)
        etFullName.setText(tvFullName.text)
        etEmail.setText(tvEmail.text)
        etPhoneNumber.setText(tvPhoneNumber.text)

        personalInfoDialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Personal Information")
            .setView(dialogView)
            .create()

        btnSubmit.setOnClickListener {
            val fullName = etFullName.text.toString()
            val email = etEmail.text.toString()
            val phone = etPhoneNumber.text.toString()
            val token = getToken() ?: return@setOnClickListener

            if (fullName.isBlank() || email.isBlank()) {
                Toast.makeText(requireContext(), "Name and Email cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val requestBody = FormBody.Builder()
                .add("token", token)
                .add("full_name", fullName)
                .add("email", email)
                .add("phone_number", phone)
                .build()

            val request = Request.Builder()
                .url(Constants.BASE_URL + "update_user_info.php")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val result = response.body?.string()
                    requireActivity().runOnUiThread {
                        try {
                            val json = JSONObject(result ?: "")
                            if (json.getBoolean("success")) {
                                Toast.makeText(requireContext(), "Information updated", Toast.LENGTH_SHORT).show()
                                personalInfoDialog?.dismiss()
                                loadUserInfo()
                            } else {
                                Toast.makeText(requireContext(), json.getString("message"), Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Invalid server response", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }

        personalInfoDialog?.show()
    }



    private fun getToken(): String? {
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }
}
