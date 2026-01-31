package com.example.kernel.data.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.kernel.MainActivity
import com.example.kernel.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var volumeRampJob: Job? = null

    companion object {
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ALARM_LABEL = "alarm_label"
        const val EXTRA_SOUND_RES_ID = "sound_res_id"
        const val EXTRA_IS_VIBRATION_ON = "is_vibration_on"
        const val EXTRA_MISSION_TYPE = "mission_type"
        const val EXTRA_DIFFICULTY = "difficulty"
        const val ACTION_STOP_ALARM = "com.example.kernel.STOP_ALARM"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "alarm_service_channel"
    }

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_ALARM) {
            stopAlarmService()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, -1L) ?: -1L
        val label = intent?.getStringExtra(EXTRA_ALARM_LABEL) ?: "Alarm"
        val soundResId = intent?.getIntExtra(EXTRA_SOUND_RES_ID, 0) ?: 0
        val isVibrationOn = intent?.getBooleanExtra(EXTRA_IS_VIBRATION_ON, true) ?: true
        val missionType = intent?.getStringExtra(EXTRA_MISSION_TYPE) ?: "NONE"
        val difficulty = intent?.getStringExtra(EXTRA_DIFFICULTY) ?: "MEDIUM"

        startForeground(NOTIFICATION_ID, createNotification(label, alarmId, missionType, difficulty))
        startAlarm(soundResId, isVibrationOn)
        launchRingActivity(alarmId, label, missionType, difficulty)

        return START_STICKY
    }

    private fun startAlarm(soundResId: Int, isVibrationOn: Boolean) {
        try {
            mediaPlayer?.release()

            val finalSoundResId = if (soundResId > 0) soundResId else R.raw.alarm_clock_90867

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                setDataSource(applicationContext, android.net.Uri.parse("android.resource://$packageName/$finalSoundResId"))
                isLooping = true
                prepare()
                setVolume(0.5f, 0.5f)
                start()

                startVolumeRamp()
            }

            if (isVibrationOn) {
                startVibration()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startVolumeRamp() {
        volumeRampJob?.cancel()
        volumeRampJob = serviceScope.launch {
            var currentVolume = 0.5f
            val targetVolume = 1.0f
            val steps = 10
            val delayMs = 3000L
            val increment = (targetVolume - currentVolume) / steps

            repeat(steps) {
                delay(delayMs)
                currentVolume += increment
                mediaPlayer?.setVolume(currentVolume, currentVolume)
            }
        }
    }

    private fun startVibration() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 1000, 1000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(pattern, 0)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Kernel::AlarmWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm ringing service"
                setSound(null, null)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun launchRingActivity(alarmId: Long, label: String, missionType: String, difficulty: String) {
        val intent = Intent(this, com.example.kernel.ui.alarmy.RingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, label)
            putExtra(EXTRA_MISSION_TYPE, missionType)
            putExtra(EXTRA_DIFFICULTY, difficulty)
        }
        startActivity(intent)
    }

    private fun createNotification(label: String, alarmId: Long, missionType: String, difficulty: String): Notification {
        val fullScreenIntent = Intent(this, com.example.kernel.ui.alarmy.RingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_ALARM_LABEL, label)
            putExtra(EXTRA_MISSION_TYPE, missionType)
            putExtra(EXTRA_DIFFICULTY, difficulty)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentIntent = PendingIntent.getActivity(
            this,
            1,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("⏰ $label")
            .setContentText("Complete mission to stop alarm")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(contentIntent)
            .build()
    }

    fun stopAlarmService() {
        volumeRampJob?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null

        wakeLock?.release()
        wakeLock = null

        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmService()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
