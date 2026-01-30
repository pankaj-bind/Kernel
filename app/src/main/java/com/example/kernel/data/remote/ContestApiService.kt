package com.example.kernel.data.remote

import com.example.kernel.data.model.Contest
import retrofit2.http.GET

/**
 * Retrofit API service for fetching competitive programming contests.
 * Using the Kontests API: https://kontests.net/api
 *
 * This is a free public API that aggregates contest data from multiple platforms:
 * - Codeforces
 * - CodeChef
 * - LeetCode
 * - AtCoder
 * - HackerRank
 * - HackerEarth
 * - And more...
 */
interface ContestApiService {

    /**
     * Get all upcoming contests from all platforms
     */
    @GET("v1/all")
    suspend fun getAllContests(): List<Contest>

    /**
     * You can also get platform-specific contests:
     * - /v1/codeforces
     * - /v1/code_chef
     * - /v1/leet_code
     * - /v1/at_coder
     * - /v1/hacker_rank
     * - /v1/hacker_earth
     */
}
