package com.example.kernel.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timeInMillis: Long,
    val label: String,
    val isEnabled: Boolean = true,
    val missionType: MissionType = MissionType.NONE,
    val daysOfWeek: List<Int> = emptyList()
)

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromMissionType(value: MissionType): String {
        return value.name
    }

    @TypeConverter
    fun toMissionType(value: String): MissionType {
        return MissionType.valueOf(value)
    }

    @TypeConverter
    fun fromDaysOfWeek(days: List<Int>): String {
        return gson.toJson(days)
    }

    @TypeConverter
    fun toDaysOfWeek(value: String): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}
