package com.example.kernel.ui.alarmy

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kernel.databinding.FragmentAlarmyBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class AlarmyFragment : Fragment() {

    private var _binding: FragmentAlarmyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlarmViewModel by viewModels()
    private lateinit var alarmAdapter: AlarmAdapter

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showTimePickerDialog()
        } else {
            Snackbar.make(
                binding.root,
                "Notification permission required for alarms",
                Snackbar.LENGTH_LONG
            ).setAction("Settings") {
                openAppSettings()
            }.show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observeAlarms()
    }

    private fun setupRecyclerView() {
        alarmAdapter = AlarmAdapter(
            onToggle = { alarm -> viewModel.toggleAlarm(alarm) },
            onDelete = { alarm -> showDeleteDialog(alarm) },
            onClick = { alarm -> showEditDialog(alarm) }
        )

        binding.recyclerViewAlarms.apply {
            adapter = alarmAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupFab() {
        binding.fabAddAlarm.setOnClickListener {
            checkPermissionsAndShowPicker()
        }
    }

    private fun checkPermissionsAndShowPicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    checkExactAlarmPermission()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showPermissionRationale()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            checkExactAlarmPermission()
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = ContextCompat.getSystemService(
                requireContext(),
                android.app.AlarmManager::class.java
            )
            if (alarmManager?.canScheduleExactAlarms() == false) {
                showExactAlarmPermissionDialog()
                return
            }
        }
        showTimePickerDialog()
    }

    private fun showExactAlarmPermissionDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permission Required")
            .setMessage("To set exact alarms, please allow this permission in settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPermissionRationale() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Notification Permission")
            .setMessage("Notifications are required to alert you when alarms go off.")
            .setPositiveButton("Grant") { _, _ ->
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                showLabelDialog(selectedHour, selectedMinute)
            },
            hour,
            minute,
            false
        ).show()
    }

    private fun showLabelDialog(hour: Int, minute: Int) {
        val editText = EditText(requireContext()).apply {
            hint = "Alarm label"
            setText("Alarm")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Label")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val label = editText.text.toString().ifEmpty { "Alarm" }
                viewModel.addAlarm(hour, minute, label)
                Toast.makeText(requireContext(), "Alarm set!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(alarm: com.example.kernel.data.local.AlarmEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Alarm")
            .setMessage("Are you sure you want to delete this alarm?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAlarm(alarm)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(alarm: com.example.kernel.data.local.AlarmEntity) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = alarm.timeInMillis
        }

        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val newCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                if (newCalendar.timeInMillis <= System.currentTimeMillis()) {
                    newCalendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                val updated = alarm.copy(
                    timeInMillis = newCalendar.timeInMillis,
                    isEnabled = true
                )
                viewModel.updateAlarm(updated)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun observeAlarms() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.alarms.collect { alarms ->
                        alarmAdapter.submitList(alarms)
                        binding.emptyState.isVisible = alarms.isEmpty() && !viewModel.isLoading.value
                        binding.recyclerViewAlarms.isVisible = alarms.isNotEmpty()
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBar.isVisible = isLoading
                    }
                }
            }
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", requireContext().packageName, null)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
