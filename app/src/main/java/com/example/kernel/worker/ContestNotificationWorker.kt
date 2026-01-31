package com.example.kernel.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.kernel.MainActivity
import com.example.kernel.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * WorkManager Worker that shows a notification 1 hour before a contest starts.
 *
 * Input Data:
 * - CONTEST_NAME: Name of the contest
 * - CONTEST_PLATFORM: Platform name (e.g., "Codeforces")
 * - CONTEST_ID: Unique contest ID
 */
@HiltWorker
class ContestNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "contest_notifications"
        const val CHANNEL_NAME = "Contest Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for upcoming programming contests"

        const val KEY_CONTEST_NAME = "contest_name"
        const val KEY_CONTEST_PLATFORM = "contest_platform"
        const val KEY_CONTEST_ID = "contest_id"

        const val WORK_NAME_PREFIX = "contest_notification_"
    }

    override suspend fun doWork(): Result {
        val contestName = inputData.getString(KEY_CONTEST_NAME) ?: return Result.failure()
        val platform = inputData.getString(KEY_CONTEST_PLATFORM) ?: return Result.failure()
        val contestId = inputData.getString(KEY_CONTEST_ID) ?: return Result.failure()

        // Create notification channel (required for Android O+)
        createNotificationChannel()

        // Show the notification
        showNotification(contestName, platform, contestId)

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                importance
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(contestName: String, platform: String, contestId: String) {
        // Intent to open app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "cp_fragment")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            contestId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_emoji_events)
            .setContentTitle("🏆 Contest Starting Soon!")
            .setContentText("$contestName starts in 1 hour on $platform!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$contestName starts in 1 hour on $platform!\n\nTap to view all contests.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        NotificationManagerCompat.from(context).notify(
            contestId.hashCode(),
            notification
        )
    }
}
