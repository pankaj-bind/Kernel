package com.example.kernel.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1)
        val label = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Alarm"
        val soundResId = intent.getIntExtra(AlarmScheduler.EXTRA_SOUND_RES_ID, 0)
        val isVibrationOn = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_VIBRATION_ON, true)
        val missionType = intent.getStringExtra(AlarmScheduler.EXTRA_MISSION_TYPE) ?: "NONE"
        val difficulty = intent.getStringExtra(AlarmScheduler.EXTRA_DIFFICULTY) ?: "MEDIUM"
        val shakeCount = intent.getIntExtra(AlarmScheduler.EXTRA_SHAKE_COUNT, 30)

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmService.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmService.EXTRA_SOUND_RES_ID, soundResId)
            putExtra(AlarmService.EXTRA_IS_VIBRATION_ON, isVibrationOn)
            putExtra(AlarmService.EXTRA_MISSION_TYPE, missionType)
            putExtra(AlarmService.EXTRA_DIFFICULTY, difficulty)
            putExtra(AlarmService.EXTRA_SHAKE_COUNT, shakeCount)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
