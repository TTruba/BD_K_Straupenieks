package com.kstraupenieks.asca

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat

class SettingsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        val prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val switchWhitelist = view.findViewById<SwitchCompat>(R.id.switchWhitelist)

        // Load and apply saved switch state
        switchWhitelist.isChecked = prefs.getBoolean("whitelist_mode", false)

        switchWhitelist.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("whitelist_mode", isChecked).apply()
            Toast.makeText(requireContext(), "Whitelist mode ${if (isChecked) "enabled" else "disabled"}", Toast.LENGTH_SHORT).show()
        }

        val etPhone = view.findViewById<EditText>(R.id.etPhoneNumber)
        val cbCalls = view.findViewById<CheckBox>(R.id.cbBlockCalls)
        val cbSms = view.findViewById<CheckBox>(R.id.cbBlockSms)
        val btnBlock = view.findViewById<Button>(R.id.btnBlockNumber)

        loadBlockedNumbers(view)

        btnBlock.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val blockCalls = cbCalls.isChecked
            val blockSms = cbSms.isChecked

            if (token == null) {
                Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.isEmpty() || (!blockCalls && !blockSms)) {
                Toast.makeText(requireContext(), "Please enter phone number and select at least one option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val url = Constants.BASE_URL + "add_blocked_number.php"

            val request = object : StringRequest(
                Request.Method.POST, url,
                { response ->
                    try {
                        val json = JSONObject(response)
                        if (json.getBoolean("success")) {
                            Toast.makeText(requireContext(), "Number blocked successfully", Toast.LENGTH_SHORT).show()
                            etPhone.text.clear()
                            cbCalls.isChecked = false
                            cbSms.isChecked = false
                            loadBlockedNumbers(view)
                        } else {
                            Toast.makeText(requireContext(), json.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Invalid server response format", Toast.LENGTH_LONG).show()
                    }
                },
                { error ->
                    Toast.makeText(requireContext(), "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    return hashMapOf(
                        "token" to token,
                        "phone_number" to phone,
                        "block_calls" to if (blockCalls) "1" else "0",
                        "block_sms" to if (blockSms) "1" else "0"
                    )
                }
            }

            Volley.newRequestQueue(requireContext()).add(request)
        }
    }

    private fun loadBlockedNumbers(rootView: View) {
        val container = rootView.findViewById<LinearLayout>(R.id.blockedNumbersContainer)
        container.removeAllViews()

        val token = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            .getString("token", null) ?: return

        val url = Constants.BASE_URL + "get_blocked_numbers.php"

        val request = object : StringRequest(Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val list = json.getJSONArray("blocked_numbers")
                        for (i in 0 until list.length()) {
                            val obj = list.getJSONObject(i)
                            val number = obj.getString("phone_number")
                            val calls = obj.getInt("block_calls") == 1
                            val sms = obj.getInt("block_sms") == 1

                            val itemLayout = LinearLayout(requireContext())
                            itemLayout.orientation = LinearLayout.HORIZONTAL
                            itemLayout.setPadding(0, 8, 0, 8)

                            val tv = TextView(requireContext())
                            tv.text = "$number - Calls: ${if (calls) "✔" else "✖"}, SMS: ${if (sms) "✔" else "✖"}"
                            tv.textSize = 16f
                            tv.setTextColor(Color.WHITE)
                            tv.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

                            val editIcon = ImageView(requireContext())
                            editIcon.setImageResource(android.R.drawable.ic_menu_edit)
                            editIcon.setColorFilter(Color.parseColor("#FF4444"))
                            editIcon.setPadding(16, 0, 0, 0)
                            editIcon.setOnClickListener {
                                showEditDialog(number, calls, sms)
                            }

                            itemLayout.addView(tv)
                            itemLayout.addView(editIcon)
                            container.addView(itemLayout)
                        }
                    } else {
                        Toast.makeText(requireContext(), json.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Failed to load blocked numbers", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("token" to token)
            }
        }

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun showEditDialog(phone: String, blockCalls: Boolean, blockSms: Boolean) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Modify Blocked Number")

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_block, null)
        val cbCall = dialogView.findViewById<CheckBox>(R.id.cbEditCalls)
        val cbSms = dialogView.findViewById<CheckBox>(R.id.cbEditSms)

        cbCall.isChecked = blockCalls
        cbSms.isChecked = blockSms

        builder.setView(dialogView)

        builder.setPositiveButton("Save") { _, _ ->
            val newBlockCalls = if (cbCall.isChecked) "1" else "0"
            val newBlockSms = if (cbSms.isChecked) "1" else "0"

            val url = Constants.BASE_URL + "update_blocked_number.php"
            val token = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                .getString("token", null) ?: return@setPositiveButton

            val request = object : StringRequest(Request.Method.POST, url,
                {
                    Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_SHORT).show()
                    loadBlockedNumbers(requireView())
                },
                { error ->
                    Toast.makeText(requireContext(), "Update failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    return hashMapOf(
                        "token" to token,
                        "phone_number" to phone,
                        "block_calls" to newBlockCalls,
                        "block_sms" to newBlockSms
                    )
                }
            }

            Volley.newRequestQueue(requireContext()).add(request)
        }

        builder.setNegativeButton("Unblock") { _, _ ->
            val url = Constants.BASE_URL + "remove_blocked_number.php"
            val token = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                .getString("token", null) ?: return@setNegativeButton

            val request = object : StringRequest(Request.Method.POST, url,
                {
                    Toast.makeText(requireContext(), "Number unblocked", Toast.LENGTH_SHORT).show()
                    loadBlockedNumbers(requireView())
                },
                { error ->
                    Toast.makeText(requireContext(), "Failed to unblock: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    return hashMapOf("token" to token, "phone_number" to phone)
                }
            }

            Volley.newRequestQueue(requireContext()).add(request)
        }

        builder.setNeutralButton("Cancel", null)
        builder.show()
    }
}
