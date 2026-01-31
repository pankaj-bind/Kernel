package com.example.kernel.data.repository

import com.example.kernel.data.alarm.AlarmScheduler
import com.example.kernel.data.local.AlarmDao
import com.example.kernel.data.local.AlarmEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler
) {

    fun getAllAlarms(): Flow<List<AlarmEntity>> = alarmDao.getAll()

    suspend fun insertAlarm(alarm: AlarmEntity): Long {
        val id = alarmDao.insert(alarm)
        val savedAlarm = alarm.copy(id = id)
        if (savedAlarm.isEnabled) {
            scheduleAlarm(savedAlarm)
        }
        return id
    }

    suspend fun updateAlarm(alarm: AlarmEntity) {
        alarmDao.update(alarm)
        if (alarm.isEnabled) {
            scheduleAlarm(alarm)
        } else {
            alarmScheduler.cancel(alarm)
        }
    }

    suspend fun deleteAlarm(alarm: AlarmEntity) {
        alarmScheduler.cancel(alarm)
        alarmDao.delete(alarm)
    }

    suspend fun toggleAlarm(alarm: AlarmEntity) {
        val updated = alarm.copy(isEnabled = !alarm.isEnabled)
        updateAlarm(updated)
    }

    private fun scheduleAlarm(alarm: AlarmEntity) {
        alarmScheduler.schedule(alarm)
    }
}
