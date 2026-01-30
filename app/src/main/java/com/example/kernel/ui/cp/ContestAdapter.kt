package com.example.kernel.ui.cp

import android.content.Intent
import android.graphics.Color
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

    inner class ContestViewHolder(
        private val binding: ItemContestCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contest: Contest) {
            with(binding) {
                // Platform badge
                textPlatform.text = contest.site
                platformBadge.setCardBackgroundColor(getPlatformColor(contest.site))

                // Contest name
                textContestName.text = contest.name

                // Start time
                textStartTime.text = formatStartTime(contest.startTime)

                // Duration
                textDuration.text = "Duration: ${contest.getFormattedDuration()}"

                // Status badge
                val (statusText, statusColor, statusBgColor) = getStatusInfo(contest)
                textStatus.text = statusText
                textStatus.setTextColor(statusColor)
                statusBadge.setCardBackgroundColor(statusBgColor)

                // Card click - open contest URL
                cardContest.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(contest.url))
                    it.context.startActivity(intent)
                }
            }
        }

        private fun getPlatformColor(platform: String): Int {
            val context = binding.root.context
            return when (platform.lowercase()) {
                "codeforces" -> ContextCompat.getColor(context, R.color.codeforces_blue)
                "codechef" -> ContextCompat.getColor(context, R.color.codechef_brown)
                "leetcode" -> ContextCompat.getColor(context, R.color.leetcode_orange)
                "atcoder" -> ContextCompat.getColor(context, R.color.atcoder_black)
                "hackerrank" -> ContextCompat.getColor(context, R.color.hackerrank_green)
                "hackerearth" -> ContextCompat.getColor(context, R.color.hackerearth_dark)
                else -> ContextCompat.getColor(context, R.color.purple_500)
            }
        }

        private fun formatStartTime(startTime: String): String {
            return try {
                val instant = Instant.parse(startTime)
                val formatter = DateTimeFormatter
                    .ofPattern("MMM dd, yyyy • hh:mm a")
                    .withZone(ZoneId.systemDefault())
                formatter.format(instant)
            } catch (e: Exception) {
                startTime
            }
        }

        private fun getStatusInfo(contest: Contest): Triple<String, Int, Int> {
            val context = binding.root.context

            if (contest.isLive()) {
                return Triple(
                    "🔴 LIVE",
                    ContextCompat.getColor(context, R.color.status_live),
                    ContextCompat.getColor(context, R.color.status_live_bg)
                )
            }

            return try {
                val instant = Instant.parse(contest.startTime)
                val now = Instant.now()
                val duration = Duration.between(now, instant)

                val statusText = when {
                    duration.isNegative -> "Started"
                    duration.toMinutes() < 60 -> "In ${duration.toMinutes()}m"
                    duration.toHours() < 24 -> "In ${duration.toHours()}h"
                    duration.toDays() < 7 -> "In ${duration.toDays()}d"
                    else -> "Upcoming"
                }

                Triple(
                    statusText,
                    ContextCompat.getColor(context, R.color.status_upcoming),
                    ContextCompat.getColor(context, R.color.status_upcoming_bg)
                )
            } catch (e: Exception) {
                Triple(
                    "Upcoming",
                    ContextCompat.getColor(context, R.color.status_upcoming),
                    ContextCompat.getColor(context, R.color.status_upcoming_bg)
                )
            }
        }
    }

    class ContestDiffCallback : DiffUtil.ItemCallback<Contest>() {
        override fun areItemsTheSame(oldItem: Contest, newItem: Contest): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Contest, newItem: Contest): Boolean {
            return oldItem == newItem
        }
    }
}
