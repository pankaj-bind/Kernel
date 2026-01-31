package com.example.kernel.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.kernel.data.model.Contest
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules notifications for upcoming contests using WorkManager.
 * Notifications are scheduled to fire 1 hour before contest start time.
 */
@Singleton
class ContestNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule notifications for a list of contests.
     * Only schedules for contests starting more than 1 hour from now.
     */
    fun scheduleNotifications(contests: List<Contest>) {
        val now = ZonedDateTime.now()
        val oneHourFromNow = now.plusHours(1)

        contests.forEach { contest ->
            // Only schedule if contest is more than 1 hour away
            if (contest.startTime.isAfter(oneHourFromNow)) {
                scheduleNotification(contest)
            }
        }
    }

    /**
     * Schedule a notification for a single contest.
     * Notification will trigger 1 hour before the contest starts.
     */
    fun scheduleNotification(contest: Contest) {
        val now = ZonedDateTime.now()

        // Calculate delay: notification should fire 1 hour before start
        val notificationTime = contest.startTime.minusHours(1)

        // Don't schedule if notification time has already passed
        if (notificationTime.isBefore(now)) {
            return
        }

        val delay = Duration.between(now, notificationTime)
        val delayMillis = delay.toMillis()

        // Create input data for the worker
        val inputData = Data.Builder()
            .putString(ContestNotificationWorker.KEY_CONTEST_NAME, contest.name)
            .putString(ContestNotificationWorker.KEY_CONTEST_PLATFORM, contest.platform.displayName)
            .putString(ContestNotificationWorker.KEY_CONTEST_ID, contest.id)
            .build()

        // Create the work request
        val workRequest = OneTimeWorkRequestBuilder<ContestNotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("contest_notification")
            .addTag(contest.id)
            .build()

        // Unique work name to avoid duplicate notifications
        val uniqueWorkName = "${ContestNotificationWorker.WORK_NAME_PREFIX}${contest.id}"

        // Enqueue the work (REPLACE if already scheduled)
        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Cancel a scheduled notification for a specific contest.
     */
    fun cancelNotification(contestId: String) {
        val uniqueWorkName = "${ContestNotificationWorker.WORK_NAME_PREFIX}$contestId"
        workManager.cancelUniqueWork(uniqueWorkName)
    }

    /**
     * Cancel all scheduled contest notifications.
     */
    fun cancelAllNotifications() {
        workManager.cancelAllWorkByTag("contest_notification")
    }

    /**
     * Get the count of pending notifications.
     */
    suspend fun getPendingNotificationCount(): Int {
        return workManager.getWorkInfosByTag("contest_notification").get()
            .count { !it.state.isFinished }
    }
}
