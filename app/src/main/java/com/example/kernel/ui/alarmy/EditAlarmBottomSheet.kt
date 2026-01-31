package com.example.kernel.ui.alarmy

import android.app.TimePickerDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.kernel.R
import com.example.kernel.data.local.AlarmEntity
import com.example.kernel.data.local.Difficulty
import com.example.kernel.data.local.MissionType
import com.example.kernel.databinding.BottomSheetCreateAlarmBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class EditAlarmBottomSheet(private val alarm: AlarmEntity) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCreateAlarmBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlarmViewModel by viewModels()

    private var selectedTimeInMillis: Long = 0
    private var selectedMissionType: MissionType = MissionType.NONE
    private var selectedDifficulty: Difficulty = Difficulty.MEDIUM
    private var selectedSoundResId: Int = 0
    private var selectedShakeCount: Int = 30
    private var previewPlayer: MediaPlayer? = null

    private val soundOptions = mutableListOf<CreateAlarmBottomSheet.SoundOption>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCreateAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSoundOptions()
        setupUIWithExistingData()
    }

    private fun loadSoundOptions() {
        try {
            val fields = R.raw::class.java.fields
            fields.forEach { field ->
                try {
                    val resId = field.getInt(null)
                    val name = field.name.replace('_', ' ').split(' ')
                        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                    soundOptions.add(CreateAlarmBottomSheet.SoundOption(resId, name))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (soundOptions.isNotEmpty() && alarm.soundResId == 0) {
                selectedSoundResId = soundOptions[0].resourceId
            } else {
                selectedSoundResId = alarm.soundResId
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupUIWithExistingData() {
        selectedTimeInMillis = alarm.timeInMillis
        selectedMissionType = alarm.missionType
        selectedDifficulty = alarm.difficulty
        selectedSoundResId = alarm.soundResId
        selectedShakeCount = alarm.shakeCount

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = alarm.timeInMillis

        binding.tvTime.text = String.format(
            "%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )

        binding.etLabel.setText(alarm.label)
        binding.etShakeCount.setText(alarm.shakeCount.toString())

        when (alarm.missionType) {
            MissionType.NONE -> {
                binding.chipNone.isChecked = true
                binding.layoutShakeSettings.visibility = View.GONE
            }
            MissionType.MATH -> {
                binding.chipMath.isChecked = true
                binding.layoutShakeSettings.visibility = View.GONE
            }
            MissionType.SHAKE -> {
                binding.chipShake.isChecked = true
                binding.layoutShakeSettings.visibility = View.VISIBLE
            }
        }

        when (alarm.difficulty) {
            Difficulty.EASY -> binding.chipEasy.isChecked = true
            Difficulty.MEDIUM -> binding.chipMedium.isChecked = true
            Difficulty.HARD -> binding.chipHard.isChecked = true
        }

        binding.switchVibration.isChecked = alarm.isVibrationOn

        binding.tvTime.setOnClickListener {
            showTimePicker()
        }

        setupMissionChips()
        setupDifficultyChips()
        setupSoundRecyclerView()
        setupSaveButton()
    }

    private fun setupMissionChips() {
        binding.chipGroupMission.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.chipNone -> {
                        selectedMissionType = MissionType.NONE
                        binding.layoutShakeSettings.visibility = View.GONE
                    }
                    R.id.chipMath -> {
                        selectedMissionType = MissionType.MATH
                        binding.layoutShakeSettings.visibility = View.GONE
                    }
                    R.id.chipShake -> {
                        selectedMissionType = MissionType.SHAKE
                        binding.layoutShakeSettings.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupDifficultyChips() {
        binding.chipGroupDifficulty.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.chipEasy -> selectedDifficulty = Difficulty.EASY
                    R.id.chipMedium -> selectedDifficulty = Difficulty.MEDIUM
                    R.id.chipHard -> selectedDifficulty = Difficulty.HARD
                }
            }
        }
    }

    private fun setupSoundRecyclerView() {
        if (soundOptions.isEmpty()) {
            binding.spinnerSound.visibility = View.GONE
            binding.btnPreviewSound.visibility = View.GONE
            return
        }

        val soundNames = soundOptions.map { it.name }
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            soundNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSound.adapter = adapter

        // Select current sound
        val currentIndex = soundOptions.indexOfFirst { it.resourceId == alarm.soundResId }
        if (currentIndex >= 0) {
            binding.spinnerSound.setSelection(currentIndex)
        }

        binding.spinnerSound.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSoundResId = soundOptions[position].resourceId
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        binding.btnPreviewSound.setOnClickListener {
            if (selectedSoundResId > 0) {
                playPreview(selectedSoundResId)
            }
        }
    }

    private fun playPreview(resourceId: Int) {
        previewPlayer?.release()
        previewPlayer = MediaPlayer.create(requireContext(), resourceId).apply {
            setVolume(0.5f, 0.5f)
            start()
            setOnCompletionListener {
                it.release()
            }
        }

        binding.root.postDelayed({
            previewPlayer?.release()
            previewPlayer = null
        }, 2000)
    }

    private fun setupSaveButton() {
        binding.btnSave.text = "Update Alarm"
        binding.btnSave.setOnClickListener {
            val shakeCount = binding.etShakeCount.text.toString().toIntOrNull() ?: 30

            val updatedAlarm = alarm.copy(
                timeInMillis = selectedTimeInMillis,
                label = binding.etLabel.text.toString().ifEmpty { "Alarm" },
                missionType = selectedMissionType,
                difficulty = selectedDifficulty,
                soundResId = selectedSoundResId,
                isVibrationOn = binding.switchVibration.isChecked,
                shakeCount = shakeCount
            )

            viewModel.updateAlarm(updatedAlarm)

            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = selectedTimeInMillis
            val timeStr = String.format("%02d:%02d", calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE))

            android.widget.Toast.makeText(
                requireContext(),
                "Alarm updated for $timeStr",
                android.widget.Toast.LENGTH_SHORT
            ).show()

            dismiss()
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedTimeInMillis

        val dialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                newCalendar.set(Calendar.MINUTE, minute)
                newCalendar.set(Calendar.SECOND, 0)
                newCalendar.set(Calendar.MILLISECOND, 0)

                // Only add a day if the selected time is genuinely in the past
                if (newCalendar.timeInMillis < System.currentTimeMillis()) {
                    newCalendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                selectedTimeInMillis = newCalendar.timeInMillis
                binding.tvTime.text = String.format("%02d:%02d", hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false // 12-hour format with AM/PM
        )

        // Use spinner mode for better UX
        try {
            val timePickerField = dialog.javaClass.getDeclaredField("mTimePicker")
            timePickerField.isAccessible = true
            val timePicker = timePickerField.get(dialog) as android.widget.TimePicker
            timePicker.descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
        } catch (e: Exception) {
            e.printStackTrace()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        previewPlayer?.release()
        previewPlayer = null
        _binding = null
    }

    companion object {
        const val TAG = "EditAlarmBottomSheet"
    }
}
