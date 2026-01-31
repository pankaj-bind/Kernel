package com.example.kernel.data.remote

import com.example.kernel.data.model.codeforces.CodeforcesContest
import com.example.kernel.data.model.codeforces.CodeforcesResponse
import com.example.kernel.data.model.codeforces.CodeforcesUser
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Codeforces API Service
 * Base URL: https://codeforces.com/api/
 */
interface CodeforcesApiService {

    /**
     * Get user information including rating
     * @param handles Semicolon-separated list of handles (e.g., "bindpankaj")
     */
    @GET("user.info")
    suspend fun getUserInfo(
        @Query("handles") handles: String
    ): CodeforcesResponse<List<CodeforcesUser>>

    /**
     * Get list of contests
     * @param gym If true, only gym contests are returned. Default is false.
     */
    @GET("contest.list")
    suspend fun getContestList(
        @Query("gym") gym: Boolean = false
    ): CodeforcesResponse<List<CodeforcesContest>>
}
