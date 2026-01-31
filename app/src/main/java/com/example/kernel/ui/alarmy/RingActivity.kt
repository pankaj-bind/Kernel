package com.example.kernel.ui.alarmy

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.kernel.data.alarm.AlarmService
import com.example.kernel.databinding.ActivityRingBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt
import kotlin.random.Random

class RingActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityRingBinding

    private var missionType = "NONE"
    private var difficulty = "MEDIUM"
    private var mathProblemsTotal = 3
    private var mathProblemsRemaining = 3
    private var shakeTarget = 30
    private var shakeCount = 0

    private var currentMathProblem: MathProblem? = null

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var lastShakeTime = 0L
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupLockScreenFlags()

        binding = ActivityRingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Prevent user from leaving this activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setTaskDescription(android.app.ActivityManager.TaskDescription.Builder()
                .setLabel("Alarm Ringing")
                .build())
        } else {
            @Suppress("DEPRECATION")
            setTaskDescription(android.app.ActivityManager.TaskDescription("Alarm Ringing"))
        }

        extractIntentData()
        setupCurrentTime()
        setupMission()
    }

    private fun setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        // Keep screen on to prevent user from escaping
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun extractIntentData() {
        val label = intent.getStringExtra(AlarmService.EXTRA_ALARM_LABEL) ?: "Alarm"
        missionType = intent.getStringExtra(AlarmService.EXTRA_MISSION_TYPE) ?: "NONE"
        difficulty = intent.getStringExtra(AlarmService.EXTRA_DIFFICULTY) ?: "MEDIUM"

        val customShakeCount = intent.getIntExtra(AlarmService.EXTRA_SHAKE_COUNT, 0)

        if (customShakeCount > 0 && missionType == "SHAKE") {
            shakeTarget = customShakeCount
        } else {
            shakeTarget = when (difficulty) {
                "EASY" -> 20
                "MEDIUM" -> 30
                "HARD" -> 50
                else -> 30
            }
        }

        mathProblemsTotal = when (difficulty) {
            "EASY" -> 2
            "MEDIUM" -> 3
            "HARD" -> 5
            else -> 3
        }
        mathProblemsRemaining = mathProblemsTotal


        binding.textAlarmLabel.text = label
    }

    private fun setupCurrentTime() {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        binding.textCurrentTime.text = timeFormat.format(Date())
    }

    private fun setupMission() {
        when (missionType) {
            "MATH" -> setupMathMission()
            "SHAKE" -> setupShakeMission()
            else -> enableDismissButton()
        }
    }

    private fun setupMathMission() {
        binding.layoutMathMission.isVisible = true
        binding.layoutShakeMission.isVisible = false

        generateNextMathProblem()

        binding.buttonSubmitAnswer.setOnClickListener {
            checkMathAnswer()
        }

        binding.editTextAnswer.requestFocus()
    }

    private fun generateNextMathProblem() {
        val range = when (difficulty) {
            "EASY" -> 1..10
            "MEDIUM" -> 1..20
            "HARD" -> 10..50
            else -> 1..20
        }

        val a = Random.nextInt(range.first, range.last)
        val b = Random.nextInt(range.first, range.last)
        val c = Random.nextInt(range.first, range.last)

        val answer = (a * b) + c
        currentMathProblem = MathProblem("$a × $b + $c = ?", answer)

        binding.textMathProblem.text = currentMathProblem?.question
        binding.textProblemsRemaining.text = "$mathProblemsRemaining problems remaining"
        binding.editTextAnswer.text?.clear()
    }

    private fun checkMathAnswer() {
        val userAnswer = binding.editTextAnswer.text?.toString()?.toIntOrNull()

        if (userAnswer == currentMathProblem?.answer) {
            mathProblemsRemaining--

            if (mathProblemsRemaining <= 0) {
                Toast.makeText(this, "✓ Mission Complete!", Toast.LENGTH_SHORT).show()
                enableDismissButton()
            } else {
                Toast.makeText(this, "✓ Correct!", Toast.LENGTH_SHORT).show()
                generateNextMathProblem()
            }
        } else {
            Toast.makeText(this, "✗ Wrong answer, try again", Toast.LENGTH_SHORT).show()
            binding.editTextAnswer.text?.clear()
        }
    }

    private fun setupShakeMission() {
        binding.layoutMathMission.isVisible = false
        binding.layoutShakeMission.isVisible = true

        binding.textShakeCount.text = shakeTarget.toString()
        binding.progressShake.max = shakeTarget
        binding.progressShake.progress = 0

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        } ?: run {
            Toast.makeText(this, "Accelerometer not available", Toast.LENGTH_SHORT).show()
            enableDismissButton()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                val currentTime = System.currentTimeMillis()

                if (currentTime - lastShakeTime > 500) {
                    val deltaX = x - lastX
                    val deltaY = y - lastY
                    val deltaZ = z - lastZ

                    val acceleration = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)

                    val threshold = when (difficulty) {
                        "EASY" -> 10f
                        "MEDIUM" -> 15f
                        "HARD" -> 20f
                        else -> 15f
                    }

                    if (acceleration > threshold) {
                        lastShakeTime = currentTime
                        shakeCount++

                        val remaining = shakeTarget - shakeCount
                        binding.textShakeCount.text = remaining.toString()
                        binding.progressShake.progress = shakeCount

                        if (shakeCount >= shakeTarget) {
                            Toast.makeText(this, "✓ Mission Complete!", Toast.LENGTH_SHORT).show()
                            sensorManager?.unregisterListener(this)
                            enableDismissButton()
                        }
                    }
                }

                lastX = x
                lastY = y
                lastZ = z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun enableDismissButton() {
        binding.layoutMathMission.isVisible = false
        binding.layoutShakeMission.isVisible = false

        binding.buttonDismiss.isEnabled = true
        binding.buttonDismiss.setOnClickListener {
            dismissAlarm()
        }
    }

    private fun dismissAlarm() {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP_ALARM
        }
        stopService(stopIntent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager?.unregisterListener(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Block back button - user must complete mission
        // Don't call super to prevent navigation
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Prevent user from leaving via home button
        // Bring activity back to front
        val intent = Intent(this, RingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
    }

    private data class MathProblem(val question: String, val answer: Int)
}
