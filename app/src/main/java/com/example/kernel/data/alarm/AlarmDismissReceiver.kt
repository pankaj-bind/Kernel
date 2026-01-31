package com.example.kernel.data.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmDismissReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)

        if (alarmId != -1L) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.cancel(alarmId.toInt())
        }
    }
}
