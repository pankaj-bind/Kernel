package com.example.kernel.data.model.codeforces

import com.google.gson.annotations.SerializedName

/**
 * Codeforces API response wrapper
 */
data class CodeforcesResponse<T>(
    @SerializedName("status")
    val status: String,

    @SerializedName("result")
    val result: T?,

    @SerializedName("comment")
    val comment: String? = null
) {
    fun isSuccess(): Boolean = status == "OK"
}

/**
 * Codeforces User Info
 */
data class CodeforcesUser(
    @SerializedName("handle")
    val handle: String,

    @SerializedName("rating")
    val rating: Int? = null,

    @SerializedName("maxRating")
    val maxRating: Int? = null,

    @SerializedName("rank")
    val rank: String? = null,

    @SerializedName("maxRank")
    val maxRank: String? = null
)

/**
 * Codeforces Contest from API
 */
data class CodeforcesContest(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: String, // CF, IOI, ICPC

    @SerializedName("phase")
    val phase: String, // BEFORE, CODING, PENDING_SYSTEM_TEST, SYSTEM_TEST, FINISHED

    @SerializedName("frozen")
    val frozen: Boolean,

    @SerializedName("durationSeconds")
    val durationSeconds: Long,

    @SerializedName("startTimeSeconds")
    val startTimeSeconds: Long? = null,

    @SerializedName("relativeTimeSeconds")
    val relativeTimeSeconds: Long? = null
) {
    /**
     * Determine if this contest is relevant for a user with given rating
     * Div. 1: rating >= 1900
     * Div. 2, 3, 4, Educational: rating < 1900
     */
    fun isRelevantForRating(userRating: Int?): Boolean {
        val rating = userRating ?: 0
        val nameLower = name.lowercase()

        return when {
            // High-rated users (>= 1900): Show Div. 1, Global, combined rounds
            rating >= 1900 -> {
                nameLower.contains("div. 1") ||
                nameLower.contains("div.1") ||
                nameLower.contains("global") ||
                (nameLower.contains("div. 1") && nameLower.contains("div. 2"))
            }
            // Lower-rated users (< 1900): Show Div. 2, 3, 4, Educational
            else -> {
                nameLower.contains("div. 2") ||
                nameLower.contains("div.2") ||
                nameLower.contains("div. 3") ||
                nameLower.contains("div.3") ||
                nameLower.contains("div. 4") ||
                nameLower.contains("div.4") ||
                nameLower.contains("educational")
            }
        }
    }

    fun isUpcoming(): Boolean = phase == "BEFORE"

    fun isLive(): Boolean = phase == "CODING"
}
