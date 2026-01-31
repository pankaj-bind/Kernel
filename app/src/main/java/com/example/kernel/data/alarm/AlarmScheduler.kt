package com.example.kernel.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.kernel.data.local.AlarmEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: AlarmEntity) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_LABEL, alarm.label)
            putExtra(EXTRA_MISSION_TYPE, alarm.missionType.name)
            putExtra(EXTRA_DIFFICULTY, alarm.difficulty.name)
            putExtra(EXTRA_SOUND_RES_ID, alarm.soundResId)
            putExtra(EXTRA_IS_VIBRATION_ON, alarm.isVibrationOn)
            putExtra(EXTRA_SHAKE_COUNT, alarm.shakeCount)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        var triggerTime = alarm.timeInMillis

        if (triggerTime <= System.currentTimeMillis()) {
            triggerTime += ONE_DAY_MILLIS
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancel(alarm: AlarmEntity) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_MISSION_TYPE = "extra_mission_type"
        const val EXTRA_DIFFICULTY = "extra_difficulty"
        const val EXTRA_SOUND_RES_ID = "extra_sound_res_id"
        const val EXTRA_IS_VIBRATION_ON = "extra_is_vibration_on"
        const val EXTRA_SHAKE_COUNT = "extra_shake_count"
        private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
