// MyApp.kt
package com.kstraupenieks.asca

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class Asca : Application() {
    override fun onCreate() {
        super.onCreate()
        createScamNotificationChannel()
    }

    private fun createScamNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "scam_alert_channel",
                "Scam Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for suspected scam calls"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
