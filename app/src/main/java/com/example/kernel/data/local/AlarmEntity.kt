package com.example.kernel.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class MissionType {
    NONE, MATH, SHAKE
}

enum class Difficulty {
    EASY, MEDIUM, HARD
}

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timeInMillis: Long,
    val label: String,
    val isEnabled: Boolean = true,
    val missionType: MissionType = MissionType.NONE,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val soundResId: Int = 0,
    val isVibrationOn: Boolean = true,
    val shakeCount: Int = 30
)

class Converters {

    @TypeConverter
    fun fromMissionType(value: MissionType): String {
        return value.name
    }

    @TypeConverter
    fun toMissionType(value: String): MissionType {
        return MissionType.valueOf(value)
    }

    @TypeConverter
    fun fromDifficulty(value: Difficulty): String {
        return value.name
    }

    @TypeConverter
    fun toDifficulty(value: String): Difficulty {
        return Difficulty.valueOf(value)
    }
}
