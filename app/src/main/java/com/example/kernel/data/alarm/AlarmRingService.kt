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
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.kernel.R
import com.example.kernel.ui.alarmy.RingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmRingService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var fadeInJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L) ?: -1L
        val label = intent?.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Alarm"
        val missionType = intent?.getStringExtra(AlarmScheduler.EXTRA_MISSION_TYPE) ?: "NONE"
        val isFadeIn = intent?.getBooleanExtra(EXTRA_FADE_IN, true) ?: true
        val difficulty = intent?.getStringExtra(EXTRA_DIFFICULTY) ?: "MEDIUM"
        val mathCount = intent?.getIntExtra(EXTRA_MATH_COUNT, 3) ?: 3
        val shakeTarget = intent?.getIntExtra(EXTRA_SHAKE_TARGET, 30) ?: 30

        startForeground(NOTIFICATION_ID, createNotification(label))

        startAlarmSound(isFadeIn)

        launchRingActivity(alarmId, label, missionType, difficulty, mathCount, shakeTarget)

        return START_STICKY
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "Kernel:AlarmRingServiceWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L)
        }
    }

    private fun startAlarmSound(isFadeIn: Boolean) {
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )

            try {
                setDataSource(applicationContext, alarmUri)
                isLooping = true

                if (isFadeIn) {
                    setVolume(0f, 0f)
                } else {
                    setVolume(1f, 1f)
                }

                prepare()
                start()

                if (isFadeIn) {
                    startFadeIn()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startFadeIn() {
        fadeInJob = serviceScope.launch {
            var currentVolume = 0f
            while (currentVolume < 1f) {
                delay(3000)
                currentVolume += 0.1f
                if (currentVolume > 1f) currentVolume = 1f
                mediaPlayer?.setVolume(currentVolume, currentVolume)
            }
        }
    }

    private fun launchRingActivity(
        alarmId: Long,
        label: String,
        missionType: String,
        difficulty: String,
        mathCount: Int,
        shakeTarget: Int
    ) {
        val intent = Intent(this, RingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmScheduler.EXTRA_MISSION_TYPE, missionType)
            putExtra(EXTRA_DIFFICULTY, difficulty)
            putExtra(EXTRA_MATH_COUNT, mathCount)
            putExtra(EXTRA_SHAKE_TARGET, shakeTarget)
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Ringing",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows when alarm is ringing"
                setSound(null, null)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(label: String): Notification {
        val intent = Intent(this, RingActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alarm Ringing")
            .setContentText(label)
            .setSmallIcon(R.drawable.ic_alarm)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()
    }

    fun stopAlarm() {
        fadeInJob?.cancel()
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
        wakeLock?.release()
        serviceScope.coroutineContext[Job]?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "alarm_ring_channel"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_FADE_IN = "extra_fade_in"
        const val EXTRA_DIFFICULTY = "extra_difficulty"
        const val EXTRA_MATH_COUNT = "extra_math_count"
        const val EXTRA_SHAKE_TARGET = "extra_shake_target"

        fun stop(context: Context) {
            context.stopService(Intent(context, AlarmRingService::class.java))
        }
    }
}
