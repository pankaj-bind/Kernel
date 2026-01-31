package com.example.kernel.ui.alarmy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kernel.data.local.AlarmEntity
import com.example.kernel.data.local.Difficulty
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

            binding.chipMission.text = getMissionText(alarm.missionType)
            binding.chipDifficulty.text = getDifficultyText(alarm.difficulty)

            // Show time until alarm for debugging
            if (alarm.isEnabled) {
                val now = System.currentTimeMillis()
                val timeUntil = alarm.timeInMillis - now
                if (timeUntil > 0) {
                    val hours = timeUntil / (1000 * 60 * 60)
                    val minutes = (timeUntil % (1000 * 60 * 60)) / (1000 * 60)
                    binding.textDays.visibility = android.view.View.VISIBLE
                    binding.textDays.text = if (hours > 0) {
                        "Rings in ${hours}h ${minutes}m"
                    } else {
                        "Rings in ${minutes}m"
                    }
                } else {
                    binding.textDays.visibility = android.view.View.VISIBLE
                    binding.textDays.text = "Alarm time passed"
                }
            } else {
                binding.textDays.visibility = android.view.View.GONE
            }

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

            binding.root.alpha = if (alarm.isEnabled) 1.0f else 0.6f
            binding.cardAlarm.strokeWidth = if (alarm.isEnabled) 2 else 0
        }

        private fun getMissionText(missionType: MissionType): String {
            return when (missionType) {
                MissionType.NONE -> "No Mission"
                MissionType.MATH -> "🧮 Math"
                MissionType.SHAKE -> "📱 Shake"
            }
        }

        private fun getDifficultyText(difficulty: Difficulty): String {
            return when (difficulty) {
                Difficulty.EASY -> "Easy"
                Difficulty.MEDIUM -> "Medium"
                Difficulty.HARD -> "Hard"
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
