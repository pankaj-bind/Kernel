package com.example.kernel.ui.alarmy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kernel.data.local.AlarmEntity
import com.example.kernel.data.local.MissionType
import com.example.kernel.databinding.ItemAlarmBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmAdapter(
    private val onToggle: (AlarmEntity) -> Unit,
    private val onDelete: (AlarmEntity) -> Unit,
    private val onClick: (AlarmEntity) -> Unit
) : ListAdapter<AlarmEntity, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlarmViewHolder(
        private val binding: ItemAlarmBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
        private val amPmFormat = SimpleDateFormat("a", Locale.getDefault())

        fun bind(alarm: AlarmEntity) {
            val date = Date(alarm.timeInMillis)

            binding.textTime.text = timeFormat.format(date)
            binding.textAmPm.text = amPmFormat.format(date).uppercase()
            binding.textLabel.text = alarm.label
            binding.textMission.text = getMissionText(alarm.missionType)
            binding.textDays.text = getDaysText(alarm.daysOfWeek)

            binding.switchAlarm.setOnCheckedChangeListener(null)
            binding.switchAlarm.isChecked = alarm.isEnabled
            binding.switchAlarm.setOnCheckedChangeListener { _, _ ->
                onToggle(alarm)
            }

            binding.root.setOnClickListener {
                onClick(alarm)
            }

            binding.root.setOnLongClickListener {
                onDelete(alarm)
                true
            }

            binding.root.alpha = if (alarm.isEnabled) 1.0f else 0.5f
        }

        private fun getMissionText(missionType: MissionType): String {
            return when (missionType) {
                MissionType.NONE -> "No Mission"
                MissionType.MATH -> "🧮 Math Problem"
                MissionType.SHAKE -> "📱 Shake Phone"
            }
        }

        private fun getDaysText(days: List<Int>): String {
            if (days.isEmpty()) return "Once"

            val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            val selectedDays = days.map { dayNames[it - 1] }

            return when {
                days.size == 7 -> "Every day"
                days == listOf(2, 3, 4, 5, 6) -> "Weekdays"
                days == listOf(1, 7) -> "Weekends"
                else -> selectedDays.joinToString(", ")
            }
        }
    }

    class AlarmDiffCallback : DiffUtil.ItemCallback<AlarmEntity>() {
        override fun areItemsTheSame(oldItem: AlarmEntity, newItem: AlarmEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AlarmEntity, newItem: AlarmEntity): Boolean {
            return oldItem == newItem
        }
    }
}
