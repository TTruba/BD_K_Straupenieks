package com.kstraupenieks.asca

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class CallReceiver : BroadcastReceiver() {
    private fun hasRequiredPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    Log.d("CallReceiver", "📞 Incoming call detected")

                    if (context != null && hasRequiredPermissions(context)) {
                        try {
                            // ⚠️ This line is what may throw SecurityException
                            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                            if (!incomingNumber.isNullOrEmpty()) {
                                checkIfScamNumber(context, incomingNumber)
                            } else {
                                Log.w("CallReceiver", "Incoming number is null or empty (possibly restricted by Android)")
                            }
                        } catch (e: SecurityException) {
                            Log.e("CallReceiver", "SecurityException while accessing phone number", e)
                        }
                    } else {
                        Log.w("CallReceiver", "Missing required permissions for incoming number")
                    }
                }

                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    Log.d("CallReceiver", "✅ Call connected")
                }

                TelephonyManager.EXTRA_STATE_IDLE -> {
                    Log.d("CallReceiver", "❌ Call ended")
                }
            }
        }
    }



    private fun checkIfScamNumber(context: Context, number: String) {
        val url = "${Constants.BASE_URL}check_scam_number.php"

        val request = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                try {
                    val json = JSONObject(response)
                    val isScam = json.optBoolean("scam", false)

                    if (json.optBoolean("success", false) && isScam) {
                        showScamNotification(context, number)
                    }
                } catch (e: Exception) {
                    Log.e("CallReceiver", "JSON error: ${e.message}")
                }
            },
            Response.ErrorListener { error ->
                Log.e("CallReceiver", "Network error: ${error.message}")
                error.networkResponse?.data?.let {
                    val errorDetails = String(it)
                    Log.e("CallReceiver", "Error details: $errorDetails")
                }
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf("phone" to number)
            }
        }

        Volley.newRequestQueue(context).add(request)
    }

    companion object {
        fun showScamNotification(context: Context, number: String, type: String = "scam") {
            val channelId = "scam_alert_channel"
            val notificationId = number.hashCode()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("CallReceiver", "Notification permission not granted")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Scam Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for blocked or scam calls"
                }

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }

            val title: String
            val message: String

            when (type) {
                "user" -> {
                    title = "📛 Blocked Number"
                    message = "Blocked number $number tried to call."
                }
                "frequency" -> {
                    title = "📞 Spam Behavior Detected"
                    message = "Caller $number made too many calls recently."
                }
                "whitelist" -> {
                    title = "🔒 Whitelist Block"
                    message = "Caller $number is not in your contacts."
                }
                else -> {
                    title = "⚠️ Scam Call Detected"
                    message = "Caller $number is flagged as scam."
                }
            }

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }



}
