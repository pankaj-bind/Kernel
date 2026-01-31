package com.example.kernel.ui.alarmy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kernel.data.local.AlarmEntity
import com.example.kernel.data.local.Difficulty
import com.example.kernel.data.local.MissionType
import com.example.kernel.data.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val repository: AlarmRepository
) : ViewModel() {

    private val _alarms = MutableStateFlow<List<AlarmEntity>>(emptyList())
    val alarms: StateFlow<List<AlarmEntity>> = _alarms.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAlarms()
    }

    private fun loadAlarms() {
        viewModelScope.launch {
            repository.getAllAlarms().collect { alarmList ->
                _alarms.value = alarmList
                _isLoading.value = false
            }
        }
    }

    fun addAlarm(
        timeInMillis: Long,
        label: String,
        missionType: MissionType = MissionType.NONE,
        difficulty: Difficulty = Difficulty.MEDIUM,
        soundResId: Int = 0,
        isVibrationOn: Boolean = true
    ) {
        viewModelScope.launch {
            val alarm = AlarmEntity(
                timeInMillis = timeInMillis,
                label = label,
                isEnabled = true,
                missionType = missionType,
                difficulty = difficulty,
                soundResId = soundResId,
                isVibrationOn = isVibrationOn
            )

            repository.insertAlarm(alarm)
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.toggleAlarm(alarm)
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
        }
    }

    fun updateAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.updateAlarm(alarm)
        }
    }
}
