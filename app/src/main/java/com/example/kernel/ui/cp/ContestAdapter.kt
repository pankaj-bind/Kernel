package com.example.kernel.ui.cp

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kernel.R
import com.example.kernel.data.model.Contest
import com.example.kernel.databinding.ItemContestCardBinding
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Data class to hold status styling information
 */
private data class StatusInfo(
    val text: String,
    val textColor: Int,
    val background: Int
)

class ContestAdapter : ListAdapter<Contest, ContestAdapter.ContestViewHolder>(ContestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContestViewHolder {
        val binding = ItemContestCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ContestViewHolder(
        private val binding: ItemContestCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contest: Contest) {
            with(binding) {
                // Platform logo
                imagePlatform.setImageResource(getPlatformLogo(contest.platform))

                // Platform name
                textPlatform.text = contest.platform.displayName.uppercase(Locale.ROOT)

                // Contest name
                textContestName.text = contest.name

                // Formatted start time with day name
                textStartTime.text = formatStartTime(contest)

                // Duration (without "Duration:" prefix for cleaner look)
                textDuration.text = contest.getFormattedDuration()

                // Status chip with dynamic background and text
                setupStatusChip(contest)

                // Card click - open contest URL
                cardContest.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(contest.url))
                    it.context.startActivity(intent)
                }
            }
        }

        /**
         * Get platform logo drawable resource
         */
        private fun getPlatformLogo(platform: Contest.Platform): Int {
            return when (platform) {
                Contest.Platform.CODEFORCES -> R.drawable.ic_codeforces
                Contest.Platform.CODECHEF -> R.drawable.ic_codechef
                Contest.Platform.LEETCODE -> R.drawable.ic_leetcode
                Contest.Platform.ATCODER -> R.drawable.ic_atcoder
                Contest.Platform.HACKERRANK -> R.drawable.ic_hackerrank
                Contest.Platform.HACKEREARTH -> R.drawable.ic_hackerearth
                else -> R.drawable.ic_emoji_events
            }
        }

        /**
         * Format start time in premium style: "Sat, 31 Jan • 08:00 PM"
         */
        private fun formatStartTime(contest: Contest): String {
            val dayOfWeek = contest.startTime.dayOfWeek
                .getDisplayName(TextStyle.SHORT, Locale.getDefault())

            val formatter = DateTimeFormatter.ofPattern("dd MMM • hh:mm a")
            val dateTime = contest.startTime.format(formatter)

            return "$dayOfWeek, $dateTime"
        }

        /**
         * Setup status chip with dynamic color and text
         */
        private fun setupStatusChip(contest: Contest) {
            val context = binding.root.context
            val statusInfo = getStatusInfo(contest)

            with(binding.textStatus) {
                text = statusInfo.text
                setTextColor(ContextCompat.getColor(context, statusInfo.textColor))
                setBackgroundResource(statusInfo.background)
            }
        }

        /**
         * Get status information (text, color, background)
         */
        private fun getStatusInfo(contest: Contest): StatusInfo {
            return if (contest.isLive()) {
                StatusInfo(
                    text = "🔴 LIVE",
                    textColor = R.color.status_live,
                    background = R.drawable.bg_status_chip_live
                )
            } else {
                val timeUntil = contest.getTimeUntilStart()

                // Determine if it's starting soon (within 6 hours)
                val isSoon = timeUntil.contains("h") && !timeUntil.contains("d")
                    && timeUntil.replace(Regex("[^0-9]"), "").toIntOrNull()?.let { it <= 6 } == true

                StatusInfo(
                    text = timeUntil,
                    textColor = if (isSoon) R.color.orange_500 else R.color.status_upcoming,
                    background = if (isSoon) R.drawable.bg_status_chip_soon
                                else R.drawable.bg_status_chip_upcoming
                )
            }
        }
    }

    class ContestDiffCallback : DiffUtil.ItemCallback<Contest>() {
        override fun areItemsTheSame(oldItem: Contest, newItem: Contest): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contest, newItem: Contest): Boolean {
            return oldItem == newItem
        }
    }
}
