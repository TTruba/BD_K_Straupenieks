package com.kstraupenieks.asca

import android.content.Context
import android.telecom.CallScreeningService
import android.telecom.CallScreeningService.CallResponse
import android.util.Log
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ScamCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: android.telecom.Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart ?: return
        Log.d("CallScreening", "Incoming call from: $number")

        val callTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val whitelistEnabled = prefs.getBoolean("whitelist_mode", false)

        if (whitelistEnabled && !isNumberInContacts(this, number)) {
            Log.d("CallScreening", "Whitelist mode enabled: $number not in contacts → rejected")

            CallReceiver.showScamNotification(this, number, "whitelist")

            respondToCall(callDetails, CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .build()
            )

            return
        }

        // Proceed with regular flow
        checkCallFrequency(this, callDetails, number, callTime)
    }

    private fun checkCallFrequency(context: Context, callDetails: android.telecom.Call.Details, number: String, callTime: String) {
        val url = "${Constants.BASE_URL}analyze_caller_behavior.php"

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    val isSpam = json.optBoolean("blocked", false)
                    val count = json.optInt("call_count", 0)

                    if (isSpam) {
                        Log.d("CallScreening", "🚫 Blocked for high volume: $count calls/hour")
                        CallReceiver.showScamNotification(context, number, "frequency")

                        // Log denied call
                        sendCallLog(context, number, pickedUp = false, denied = true, duration = 0, source = "screened_frequency", callTime)

                        respondToCall(callDetails, CallResponse.Builder()
                            .setDisallowCall(true)
                            .setRejectCall(true)
                            .build()
                        )
                    } else {
                        checkIfUserBlockedAndHandle(context, callDetails, number, callTime)
                    }
                } catch (e: Exception) {
                    checkIfUserBlockedAndHandle(context, callDetails, number, callTime)
                }
            },
            { _ ->
                checkIfUserBlockedAndHandle(context, callDetails, number, callTime)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "phone" to number,
                    "picked_up" to "0",
                    "denied" to "0",
                    "duration" to "0",
                    "source" to "screened_check",
                    "call_time" to callTime
                )
            }
        }

        Volley.newRequestQueue(context).add(request)
    }

    private fun checkIfUserBlockedAndHandle(context: Context, callDetails: android.telecom.Call.Details, number: String, callTime: String) {
        val url = "${Constants.BASE_URL}check_user_blocked_number.php"

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    val isBlocked = json.optBoolean("blocked", false)

                    if (json.optBoolean("success", false) && isBlocked) {
                        Log.d("CallScreening", "📛 Blocked by user")

                        CallReceiver.showScamNotification(context, number, "user")

                        // Log denied call
                        sendCallLog(context, number, pickedUp = false, denied = true, duration = 0, source = "blocked_by_user", callTime)

                        respondToCall(callDetails, CallResponse.Builder()
                            .setDisallowCall(true)
                            .setRejectCall(true)
                            .build()
                        )
                    } else {
                        checkIfScamNumber(context, callDetails, number, callTime)
                    }
                } catch (e: Exception) {
                    checkIfScamNumber(context, callDetails, number, callTime)
                }
            },
            { _ ->
                checkIfScamNumber(context, callDetails, number, callTime)
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "phone" to number,
                    "username" to getUsername(context)
                )
            }
        }

        Volley.newRequestQueue(context).add(request)
    }

    private fun checkIfScamNumber(context: Context, callDetails: android.telecom.Call.Details, number: String, callTime: String) {
        val url = "${Constants.BASE_URL}check_scam_number.php"

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    val isScam = json.optBoolean("scam", false)

                    if (json.optBoolean("success", false) && isScam) {
                        Log.d("CallScreening", "🚫 Scam detected. Rejecting call.")
                        CallReceiver.showScamNotification(context, number, "scam")

                        sendCallLog(context, number, pickedUp = false, denied = true, duration = 0, source = "scam_blocked", callTime)

                        respondToCall(callDetails, CallResponse.Builder()
                            .setDisallowCall(true)
                            .setRejectCall(true)
                            .build()
                        )
                    } else {
                        respondToCall(callDetails, CallResponse.Builder()
                            .setDisallowCall(false)
                            .build()
                        )
                    }
                } catch (e: Exception) {
                    respondToCall(callDetails, CallResponse.Builder().setDisallowCall(false).build())
                }
            },
            { _ ->
                respondToCall(callDetails, CallResponse.Builder().setDisallowCall(false).build())
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("phone" to number)
            }
        }

        Volley.newRequestQueue(context).add(request)
    }
    private fun isNumberInContacts(context: Context, number: String): Boolean {
        val contentResolver = context.contentResolver
        val uri = android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI
            .buildUpon()
            .appendPath(number)
            .build()

        contentResolver.query(uri, arrayOf(android.provider.ContactsContract.PhoneLookup._ID), null, null, null)
            .use { cursor ->
                return cursor != null && cursor.moveToFirst()
            }
    }

    private fun sendCallLog(
        context: Context,
        number: String,
        pickedUp: Boolean,
        denied: Boolean,
        duration: Int,
        source: String,
        callTime: String
    ) {
        val url = "${Constants.BASE_URL}analyze_caller_behavior.php"

        val request = object : StringRequest(Method.POST, url,
            { response -> Log.d("CallLog", "Log sent: $response") },
            { error -> Log.e("CallLog", "Log error: ${error.message}") }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "phone" to number,
                    "picked_up" to if (pickedUp) "1" else "0",
                    "denied" to if (denied) "1" else "0",
                    "duration" to duration.toString(),
                    "source" to source,
                    "call_time" to callTime
                )
            }
        }

        Volley.newRequestQueue(context).add(request)
    }

    private fun getUsername(context: Context): String {
        val prefs = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return prefs.getString("username", "") ?: ""
    }
}
