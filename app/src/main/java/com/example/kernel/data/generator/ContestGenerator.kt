package com.example.kernel.data.generator

import com.example.kernel.data.model.Contest
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates upcoming contests for platforms with fixed schedules:
 * - CodeChef Starters: Every Wednesday at 8:00 PM IST
 * - LeetCode Weekly: Every Sunday at 8:00 AM IST
 * - LeetCode Biweekly: Every alternate Saturday at 8:00 PM IST
 */
@Singleton
class ContestGenerator @Inject constructor() {

    companion object {
        // IST Timezone
        private val IST_ZONE = ZoneId.of("Asia/Kolkata")

        // ========== SEED DATA ==========
        // CodeChef Starters 224 on 04/02/2026 (Wednesday)
        private val CODECHEF_SEED_DATE = LocalDate.of(2026, 2, 4)
        private const val CODECHEF_SEED_NUMBER = 224
        private val CODECHEF_TIME = LocalTime.of(20, 0) // 8:00 PM IST
        private const val CODECHEF_DURATION_SECONDS = 2L * 60 * 60 // 2 hours

        // LeetCode Weekly Contest 487 on 01/02/2026 (Sunday - corrected)
        private val LEETCODE_WEEKLY_SEED_DATE = LocalDate.of(2026, 2, 1)
        private const val LEETCODE_WEEKLY_SEED_NUMBER = 487
        private val LEETCODE_WEEKLY_TIME = LocalTime.of(8, 0) // 8:00 AM IST
        private const val LEETCODE_WEEKLY_DURATION_SECONDS = 90L * 60 // 1.5 hours

        // LeetCode Biweekly Contest 175 on 31/01/2026 (Saturday - TODAY)
        private val LEETCODE_BIWEEKLY_SEED_DATE = LocalDate.of(2026, 1, 31)
        private const val LEETCODE_BIWEEKLY_SEED_NUMBER = 175
        private val LEETCODE_BIWEEKLY_TIME = LocalTime.of(20, 0) // 8:00 PM IST
        private const val LEETCODE_BIWEEKLY_DURATION_SECONDS = 90L * 60 // 1.5 hours
    }

    /**
     * Generate all upcoming local contests (CodeChef + LeetCode)
     * Returns contests for the next 4 weeks
     */
    fun generateUpcomingContests(): List<Contest> {
        val contests = mutableListOf<Contest>()

        try {
            // Generate next 4 CodeChef Starters
            contests.addAll(generateCodeChefStarters(4))

            // Generate next 4 LeetCode Weekly contests
            contests.addAll(generateLeetCodeWeekly(4))

            // Generate next 2 LeetCode Biweekly contests
            contests.addAll(generateLeetCodeBiweekly(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return contests.sortedBy { it.startTime }
    }

    /**
     * Generate upcoming CodeChef Starters contests
     * Schedule: Every Wednesday at 8:00 PM IST
     */
    fun generateCodeChefStarters(count: Int): List<Contest> {
        val contests = mutableListOf<Contest>()

        try {
            val now = ZonedDateTime.now(IST_ZONE)

            // Find the next Wednesday
            var nextWednesday = now.toLocalDate()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY))

            // If today is Wednesday but contest time has passed, move to next week
            if (nextWednesday == now.toLocalDate() &&
                now.toLocalTime().isAfter(CODECHEF_TIME)) {
                nextWednesday = nextWednesday.plusWeeks(1)
            }

            // Calculate the contest number based on weeks since seed
            val weeksSinceSeed = ChronoUnit.WEEKS.between(CODECHEF_SEED_DATE, nextWednesday)
            var contestNumber = CODECHEF_SEED_NUMBER + weeksSinceSeed.toInt()

            // Adjust if seed date is in the future
            if (nextWednesday.isBefore(CODECHEF_SEED_DATE)) {
                val weeksUntilSeed = ChronoUnit.WEEKS.between(nextWednesday, CODECHEF_SEED_DATE)
                contestNumber = CODECHEF_SEED_NUMBER - weeksUntilSeed.toInt()
            }

            for (i in 0 until count) {
                val contestDate = nextWednesday.plusWeeks(i.toLong())
                val startTime = ZonedDateTime.of(contestDate, CODECHEF_TIME, IST_ZONE)

                // Only add future contests
                if (startTime.isAfter(now)) {
                    contests.add(
                        Contest(
                            id = "codechef_starters_${contestNumber + i}",
                            name = "Starters ${contestNumber + i}",
                            url = "https://www.codechef.com/START${contestNumber + i}",
                            startTime = startTime,
                            durationSeconds = CODECHEF_DURATION_SECONDS,
                            platform = Contest.Platform.CODECHEF
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Return empty list on error - don't crash the app
            e.printStackTrace()
        }

        return contests
    }

    /**
     * Generate upcoming LeetCode Weekly contests
     * Schedule: Every Sunday at 8:00 AM IST
     */
    fun generateLeetCodeWeekly(count: Int): List<Contest> {
        val contests = mutableListOf<Contest>()

        try {
            val now = ZonedDateTime.now(IST_ZONE)

            // Find the next Sunday (or adjust seed to nearest Sunday)
            val seedSunday = LEETCODE_WEEKLY_SEED_DATE.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

            // Find the next Sunday from now
            var nextSunday = now.toLocalDate()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

            // If today is Sunday but contest time has passed, move to next week
            if (nextSunday == now.toLocalDate() &&
                now.toLocalTime().isAfter(LEETCODE_WEEKLY_TIME)) {
                nextSunday = nextSunday.plusWeeks(1)
            }

            // Calculate the contest number based on weeks since seed
            val weeksSinceSeed = ChronoUnit.WEEKS.between(seedSunday, nextSunday)
            var contestNumber = LEETCODE_WEEKLY_SEED_NUMBER + weeksSinceSeed.toInt()

            // Adjust if seed date is in the future
            if (nextSunday.isBefore(seedSunday)) {
                val weeksUntilSeed = ChronoUnit.WEEKS.between(nextSunday, seedSunday)
                contestNumber = LEETCODE_WEEKLY_SEED_NUMBER - weeksUntilSeed.toInt()
            }

            for (i in 0 until count) {
                val contestDate = nextSunday.plusWeeks(i.toLong())
                val startTime = ZonedDateTime.of(contestDate, LEETCODE_WEEKLY_TIME, IST_ZONE)

                // Only add future contests
                if (startTime.isAfter(now)) {
                    contests.add(
                        Contest(
                            id = "leetcode_weekly_${contestNumber + i}",
                            name = "Weekly Contest ${contestNumber + i}",
                            url = "https://leetcode.com/contest/weekly-contest-${contestNumber + i}",
                            startTime = startTime,
                            durationSeconds = LEETCODE_WEEKLY_DURATION_SECONDS,
                            platform = Contest.Platform.LEETCODE
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Return empty list on error - don't crash the app
            e.printStackTrace()
        }

        return contests
    }

    /**
     * Generate upcoming LeetCode Biweekly contests
     * Schedule: Every alternate Saturday at 8:00 PM IST (every 14 days)
     */
    fun generateLeetCodeBiweekly(count: Int): List<Contest> {
        val contests = mutableListOf<Contest>()

        try {
            val now = ZonedDateTime.now(IST_ZONE)
            val today = now.toLocalDate()

            // Calculate days since seed date
            val daysSinceSeed = ChronoUnit.DAYS.between(LEETCODE_BIWEEKLY_SEED_DATE, today)

            // Find the next biweekly contest date
            // Biweekly runs every 14 days from the seed date
            val daysUntilNext = if (daysSinceSeed >= 0) {
                val remainder = daysSinceSeed % 14
                if (remainder == 0L && now.toLocalTime().isBefore(LEETCODE_BIWEEKLY_TIME)) {
                    0L // Today is biweekly day and contest hasn't started
                } else if (remainder == 0L) {
                    14L // Today is biweekly day but contest time passed
                } else {
                    14L - remainder
                }
            } else {
                // Seed date is in the future
                -daysSinceSeed % 14
            }

            var nextBiweeklyDate = today.plusDays(daysUntilNext)

            // Calculate contest number
            val biweeklyPeriodsSinceSeed = ChronoUnit.DAYS.between(
                LEETCODE_BIWEEKLY_SEED_DATE, nextBiweeklyDate
            ) / 14
            val contestNumber = LEETCODE_BIWEEKLY_SEED_NUMBER + biweeklyPeriodsSinceSeed.toInt()

            for (i in 0 until count) {
                val contestDate = nextBiweeklyDate.plusDays((i * 14).toLong())
                val startTime = ZonedDateTime.of(contestDate, LEETCODE_BIWEEKLY_TIME, IST_ZONE)

                // Only add future contests
                if (startTime.isAfter(now)) {
                    contests.add(
                        Contest(
                            id = "leetcode_biweekly_${contestNumber + i}",
                            name = "Biweekly Contest ${contestNumber + i}",
                            url = "https://leetcode.com/contest/biweekly-contest-${contestNumber + i}",
                            startTime = startTime,
                            durationSeconds = LEETCODE_BIWEEKLY_DURATION_SECONDS,
                            platform = Contest.Platform.LEETCODE
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Return empty list on error - don't crash the app
            e.printStackTrace()
        }

        return contests
    }
}
