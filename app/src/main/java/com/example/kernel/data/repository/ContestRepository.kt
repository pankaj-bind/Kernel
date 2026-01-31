package com.example.kernel.data.repository

import com.example.kernel.data.generator.ContestGenerator
import com.example.kernel.data.model.Contest
import com.example.kernel.data.model.codeforces.CodeforcesContest
import com.example.kernel.data.remote.CodeforcesApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hybrid Contest Repository that:
 * 1. Fetches Codeforces contests from real API (filtered by user rating)
 * 2. Generates CodeChef & LeetCode contests algorithmically
 * 3. Merges and sorts all contests by start time
 */
@Singleton
class ContestRepository @Inject constructor(
    private val codeforcesApi: CodeforcesApiService,
    private val contestGenerator: ContestGenerator
) {
    // Default Codeforces handle for fetching user rating
    private val defaultHandle = "bindpankaj"

    // Cached user rating
    private var cachedUserRating: Int? = null

    /**
     * Fetch all upcoming contests from all sources
     */
    fun getAllContests(): Flow<Result<List<Contest>>> = flow {
        try {
            val allContests = mutableListOf<Contest>()

            // 1. Fetch Codeforces contests (filtered by user rating)
            val codeforcesContests = fetchCodeforcesContests()
            allContests.addAll(codeforcesContests)

            // 2. Generate local contests (CodeChef + LeetCode)
            val generatedContests = contestGenerator.generateUpcomingContests()
            allContests.addAll(generatedContests)

            // 3. Sort by start time and filter only upcoming/live
            val sortedContests = allContests
                .filter { it.startTime.isAfter(ZonedDateTime.now().minusHours(1)) }
                .sortedBy { it.startTime }

            emit(Result.success(sortedContests))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Fetch Codeforces contests filtered by user rating
     */
    private suspend fun fetchCodeforcesContests(): List<Contest> {
        return try {
            // Step A: Get user rating (with caching)
            val userRating = getUserRating()

            // Step B: Fetch contest list
            val contestResponse = codeforcesApi.getContestList(gym = false)

            if (!contestResponse.isSuccess() || contestResponse.result == null) {
                return emptyList()
            }

            // Step C: Filter and convert contests
            contestResponse.result
                .filter { it.isUpcoming() || it.isLive() }
                .filter { it.startTimeSeconds != null }
                .filter { it.isRelevantForRating(userRating) }
                .map { it.toContest() }
        } catch (e: Exception) {
            // Return empty list on error, don't fail the whole request
            emptyList()
        }
    }

    /**
     * Get user rating from Codeforces API (cached)
     */
    private suspend fun getUserRating(): Int? {
        if (cachedUserRating != null) {
            return cachedUserRating
        }

        return try {
            val response = codeforcesApi.getUserInfo(defaultHandle)
            if (response.isSuccess() && !response.result.isNullOrEmpty()) {
                cachedUserRating = response.result.first().rating
                cachedUserRating
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get contests by specific platform
     */
    fun getContestsByPlatform(platform: Contest.Platform): Flow<Result<List<Contest>>> = flow {
        try {
            val contests = when (platform) {
                Contest.Platform.CODEFORCES -> fetchCodeforcesContests()
                Contest.Platform.CODECHEF -> contestGenerator.generateCodeChefStarters(4)
                Contest.Platform.LEETCODE -> {
                    contestGenerator.generateLeetCodeWeekly(4) +
                    contestGenerator.generateLeetCodeBiweekly(2)
                }
                else -> emptyList()
            }

            val sortedContests = contests
                .filter { it.startTime.isAfter(ZonedDateTime.now()) }
                .sortedBy { it.startTime }

            emit(Result.success(sortedContests))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Clear cached user rating (useful for handle changes)
     */
    fun clearUserRatingCache() {
        cachedUserRating = null
    }

    /**
     * Update the Codeforces handle and refresh rating
     */
    suspend fun updateCodeforcesHandle(handle: String): Result<Int?> {
        return try {
            val response = codeforcesApi.getUserInfo(handle)
            if (response.isSuccess() && !response.result.isNullOrEmpty()) {
                cachedUserRating = response.result.first().rating
                Result.success(cachedUserRating)
            } else {
                Result.failure(Exception(response.comment ?: "Failed to fetch user info"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extension function to convert CodeforcesContest to Contest
     */
    private fun CodeforcesContest.toContest(): Contest {
        val startDateTime = startTimeSeconds?.let {
            ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(it),
                ZoneId.systemDefault()
            )
        } ?: ZonedDateTime.now()

        val status = when {
            isLive() -> Contest.ContestStatus.LIVE
            isUpcoming() -> Contest.ContestStatus.UPCOMING
            else -> Contest.ContestStatus.FINISHED
        }

        return Contest(
            id = "codeforces_$id",
            name = name,
            url = "https://codeforces.com/contest/$id",
            startTime = startDateTime,
            durationSeconds = durationSeconds,
            platform = Contest.Platform.CODEFORCES,
            status = status
        )
    }
}
