package com.example.kernel.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data model representing a competitive programming contest.
 * This model is used to parse data from the Kontests API.
 */
data class Contest(
    @SerializedName("name")
    val name: String,

    @SerializedName("url")
    val url: String,

    @SerializedName("start_time")
    val startTime: String, // ISO 8601 format

    @SerializedName("end_time")
    val endTime: String, // ISO 8601 format

    @SerializedName("duration")
    val duration: String, // Duration in seconds

    @SerializedName("site")
    val site: String, // Platform name (e.g., "CodeForces", "CodeChef")

    @SerializedName("in_24_hours")
    val in24Hours: String, // "Yes" or "No"

    @SerializedName("status")
    val status: String // "CODING", "BEFORE"
) {
    /**
     * Get the duration in a human-readable format
     */
    fun getFormattedDuration(): String {
        val durationSeconds = duration.toLongOrNull() ?: 0
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
     * Get platform color as Int for XML views
     */
    fun getPlatformColorInt(): Int {
        return when (site.lowercase()) {
            "codeforces" -> 0xFF1976D2.toInt()
            "codechef" -> 0xFF8B4513.toInt()
            "leetcode" -> 0xFFFFA116.toInt()
            "atcoder" -> 0xFF222222.toInt()
            "hackerrank" -> 0xFF00EA64.toInt()
            "hackerearth" -> 0xFF323754.toInt()
            else -> 0xFF6200EE.toInt()
        }
    }

    /**
     * Check if contest is live
     */
    fun isLive(): Boolean = status == "CODING"
}
