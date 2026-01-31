package com.example.kernel.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.kernel.data.local.AlarmDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmDao: AlarmDao

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAlarms()
        }
    }

    private fun rescheduleAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            val enabledAlarms = alarmDao.getEnabledAlarms()
            enabledAlarms.forEach { alarm ->
                if (alarm.daysOfWeek.isEmpty()) {
                    alarmScheduler.schedule(alarm)
                } else {
                    alarmScheduler.scheduleRepeating(alarm)
                }
            }
        }
    }
}
