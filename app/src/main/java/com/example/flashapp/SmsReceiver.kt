package com.example.flashapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver(private val smsListener: SmsListener) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action.equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            smsListener.onSmsReceived()
            Log.d("232323", "sms")
        }
    }

    interface SmsListener {
        fun onSmsReceived()
    }
}