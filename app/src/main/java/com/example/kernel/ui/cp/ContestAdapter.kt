package com.example.kernel.ui.cp

import android.content.Intent
import android.content.res.ColorStateList
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
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Status information for contest chip styling
 */
private data class StatusInfo(
    val text: String,
    val textColorRes: Int
)

/**
 * Platform visual configuration
 */
private data class PlatformConfig(
    val logoRes: Int,
    val tintColorRes: Int
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
            val context = binding.root.context

            with(binding) {
                // Get platform configuration
                val platformConfig = getPlatformConfig(contest.platform)

                // Set platform logo
                imagePlatform.setImageResource(platformConfig.logoRes)

                // Set background tint color for the logo container
                val tintColor = ContextCompat.getColor(context, platformConfig.tintColorRes)
                logoBg.backgroundTintList = ColorStateList.valueOf(tintColor)

                // Platform name (uppercase)
                textPlatform.text = contest.platform.displayName.uppercase(Locale.ROOT)

                // Contest name
                textContestName.text = contest.name

                // Formatted start time: "Sat, 31 Jan • 08:00 PM"
                textStartTime.text = formatStartTime(contest)

                // Duration
                textDuration.text = contest.getFormattedDuration()

                // Status chip with dynamic styling
                val statusInfo = getStatusInfo(contest)
                textStatus.text = statusInfo.text
                val statusColor = ContextCompat.getColor(context, statusInfo.textColorRes)
                textStatus.setTextColor(statusColor)

                // Card click - open contest URL
                cardContest.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(contest.url))
                    context.startActivity(intent)
                }
            }
        }

        /**
         * Get platform-specific logo and background configuration
         */
        private fun getPlatformConfig(platform: Contest.Platform): PlatformConfig {
            return when (platform) {
                Contest.Platform.CODEFORCES -> PlatformConfig(
                    R.drawable.ic_codeforces,
                    R.color.codeforces_blue
                )
                Contest.Platform.LEETCODE -> PlatformConfig(
                    R.drawable.ic_leetcode,
                    R.color.leetcode_orange
                )
                Contest.Platform.CODECHEF -> PlatformConfig(
                    R.drawable.ic_codechef,
                    R.color.codechef_brown
                )
                Contest.Platform.ATCODER -> PlatformConfig(
                    R.drawable.ic_atcoder,
                    R.color.atcoder_black
                )
                Contest.Platform.HACKERRANK -> PlatformConfig(
                    R.drawable.ic_hackerrank,
                    R.color.hackerrank_green
                )
                Contest.Platform.HACKEREARTH -> PlatformConfig(
                    R.drawable.ic_hackerearth,
                    R.color.hackerearth_dark
                )
                else -> PlatformConfig(
                    R.drawable.ic_emoji_events,
                    R.color.purple_500
                )
            }
        }

        /**
         * Format start time: "Sat, 31 Jan • 08:00 PM"
         */
        private fun formatStartTime(contest: Contest): String {
            val dayOfWeek = contest.startTime.dayOfWeek
                .getDisplayName(TextStyle.SHORT, Locale.getDefault())

            val formatter = DateTimeFormatter.ofPattern("dd MMM • hh:mm a")
            val dateTime = contest.startTime.format(formatter)

            return "$dayOfWeek, $dateTime"
        }

        /**
         * Get status info with text, colors based on time until start
         */
        private fun getStatusInfo(contest: Contest): StatusInfo {
            val now = ZonedDateTime.now()

            // Check if LIVE
            if (contest.isLive()) {
                return StatusInfo(
                    text = "🔴 LIVE",
                    textColorRes = R.color.status_live
                )
            }

            // Calculate time until start
            val duration = Duration.between(now, contest.startTime)
            val hoursUntil = duration.toHours()
            val minutesUntil = duration.toMinutes()

            return when {
                // Starting very soon (< 1 hour) - RED
                minutesUntil in 0..59 -> StatusInfo(
                    text = if (minutesUntil <= 0) "Starting" else "In ${minutesUntil}m",
                    textColorRes = R.color.status_live
                )

                // Starting soon (1-6 hours) - ORANGE
                hoursUntil in 1..6 -> StatusInfo(
                    text = "In ${hoursUntil}h",
                    textColorRes = R.color.orange_500
                )

                // Starting today (6-24 hours) - GREEN
                hoursUntil in 7..24 -> StatusInfo(
                    text = "In ${hoursUntil}h",
                    textColorRes = R.color.status_upcoming
                )

                // Days away - GREEN
                else -> {
                    val daysUntil = duration.toDays()
                    StatusInfo(
                        text = if (daysUntil == 1L) "In 1d" else "In ${daysUntil}d",
                        textColorRes = R.color.status_upcoming
                    )
                }
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
