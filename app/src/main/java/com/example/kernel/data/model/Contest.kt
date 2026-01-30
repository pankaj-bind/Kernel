package com.example.kernel.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data model representing a competitive programming contest.
 * This model is used to parse data from the Kontests API.
 */
@JsonClass(generateAdapter = true)
data class Contest(
    @Json(name = "name")
    val name: String,

    @Json(name = "url")
    val url: String,

    @Json(name = "start_time")
    val startTime: String, // ISO 8601 format

    @Json(name = "end_time")
    val endTime: String, // ISO 8601 format

    @Json(name = "duration")
    val duration: String, // Duration in seconds

    @Json(name = "site")
    val site: String, // Platform name (e.g., "CodeForces", "CodeChef")

    @Json(name = "in_24_hours")
    val in24Hours: String, // "Yes" or "No"

    @Json(name = "status")
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
     * Get platform icon resource ID based on platform name
     */
    fun getPlatformColor(): Long {
        return when (site.lowercase()) {
            "codeforces" -> 0xFF1976D2
            "codechef" -> 0xFF8B4513
            "leetcode" -> 0xFFFFA116
            "atcoder" -> 0xFF000000
            "hackerrank" -> 0xFF00EA64
            "hackerearth" -> 0xFF323754
            else -> 0xFF6200EE
        }
    }
}
