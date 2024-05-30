package com.example.realtimenotes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class MailScheduler(private val context: Context) {

    fun scheduleMailSending(to: String, subject: String, body: String, delay: Long) {
        val intent = Intent(context, MailReciver::class.java).apply {
            putExtra("to", to)
            putExtra("subject", subject)
            putExtra("body", body)
        }

        // Add FLAG_IMMUTABLE to the PendingIntent
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + delay,
            pendingIntent
        )
    }
}
