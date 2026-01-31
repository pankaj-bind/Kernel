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

    fun scheduleRepeating(alarm: AlarmEntity) {
        if (alarm.daysOfWeek.isEmpty()) {
            schedule(alarm)
            return
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = alarm.timeInMillis
        }

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        alarm.daysOfWeek.forEach { dayOfWeek ->
            val nextTrigger = getNextTriggerTime(dayOfWeek, hour, minute)
            val repeatingAlarm = alarm.copy(
                id = alarm.id * 10 + dayOfWeek,
                timeInMillis = nextTrigger
            )
            schedule(repeatingAlarm)
        }
    }

    private fun getNextTriggerTime(dayOfWeek: Int, hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        var daysUntilTarget = dayOfWeek - currentDayOfWeek

        if (daysUntilTarget < 0 || (daysUntilTarget == 0 && calendar.timeInMillis <= System.currentTimeMillis())) {
            daysUntilTarget += 7
        }

        calendar.add(Calendar.DAY_OF_YEAR, daysUntilTarget)
        return calendar.timeInMillis
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_ALARM_LABEL = "extra_alarm_label"
        const val EXTRA_MISSION_TYPE = "extra_mission_type"
        private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
