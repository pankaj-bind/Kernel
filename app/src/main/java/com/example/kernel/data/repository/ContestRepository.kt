package com.example.kernel.data.repository

import com.example.kernel.data.model.Contest
import com.example.kernel.data.remote.ContestApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing contest data.
 * Follows the Repository pattern to abstract data sources.
 */
@Singleton
class ContestRepository @Inject constructor(
    private val apiService: ContestApiService
) {

    /**
     * Fetch all upcoming contests from the API.
     * Returns a Flow for reactive data handling.
     */
    fun getAllContests(): Flow<Result<List<Contest>>> = flow {
        try {
            val contests = apiService.getAllContests()
            // Filter and sort contests
            val filteredContests = contests
                .filter { it.status == "BEFORE" || it.status == "CODING" }
                .sortedBy { it.startTime }
            emit(Result.success(filteredContests))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Get contests filtered by specific platforms
     */
    fun getContestsByPlatforms(platforms: List<String>): Flow<Result<List<Contest>>> = flow {
        try {
            val contests = apiService.getAllContests()
            val filteredContests = contests
                .filter { contest ->
                    platforms.any { platform ->
                        contest.site.contains(platform, ignoreCase = true)
                    }
                }
                .filter { it.status == "BEFORE" || it.status == "CODING" }
                .sortedBy { it.startTime }
            emit(Result.success(filteredContests))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
