package com.example.realtimenotes

import SendMail
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MailReciver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val to = intent.getStringExtra("to")
        val subject = intent.getStringExtra("subject")
        val body = intent.getStringExtra("body")
        SendMail(to!!,subject!!,body!!)
    }
}