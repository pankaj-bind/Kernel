package com.example.kernel.data.model

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration

/**
 * Unified Contest data model supporting both API responses and locally generated contests.
 */
data class Contest(
    val id: String,
    val name: String,
    val url: String,
    val startTime: ZonedDateTime,
    val durationSeconds: Long,
    val platform: Platform,
    val status: ContestStatus = ContestStatus.UPCOMING
) {

    enum class Platform(val displayName: String, val color: Int) {
        CODEFORCES("Codeforces", 0xFF1976D2.toInt()),
        CODECHEF("CodeChef", 0xFF8B4513.toInt()),
        LEETCODE("LeetCode", 0xFFFFA116.toInt()),
        ATCODER("AtCoder", 0xFF222222.toInt()),
        HACKERRANK("HackerRank", 0xFF00EA64.toInt()),
        HACKEREARTH("HackerEarth", 0xFF323754.toInt()),
        OTHER("Other", 0xFF6200EE.toInt())
    }

    enum class ContestStatus {
        UPCOMING,
        LIVE,
        FINISHED
    }

    /**
     * Get the duration in a human-readable format
     */
    fun getFormattedDuration(): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "N/A"
        }
    }

    /**
     * Get formatted start time for display
     */
    fun getFormattedStartTime(): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm a")
        return startTime.format(formatter)
    }

    /**
     * Get time until contest starts
     */
    fun getTimeUntilStart(): String {
        val now = ZonedDateTime.now()

        if (startTime.isBefore(now)) {
            return if (status == ContestStatus.LIVE) "🔴 LIVE" else "Started"
        }

        val duration = Duration.between(now, startTime)

        return when {
            duration.toMinutes() < 60 -> "In ${duration.toMinutes()}m"
            duration.toHours() < 24 -> "In ${duration.toHours()}h"
            duration.toDays() < 7 -> "In ${duration.toDays()}d"
            else -> "Upcoming"
        }
    }

    /**
     * Check if contest is currently live
     */
    fun isLive(): Boolean {
        val now = ZonedDateTime.now()
        val endTime = startTime.plusSeconds(durationSeconds)
        return now.isAfter(startTime) && now.isBefore(endTime)
    }

    /**
     * Check if contest is upcoming (within next 7 days)
     */
    fun isUpcoming(): Boolean {
        val now = ZonedDateTime.now()
        val weekLater = now.plusDays(7)
        return startTime.isAfter(now) && startTime.isBefore(weekLater)
    }
}
