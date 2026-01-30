package com.example.kernel.ui.cp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kernel.data.model.Contest
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * A card component that displays information about a competitive programming contest.
 *
 * @param contest The contest data to display
 * @param onClick Callback when the card is clicked
 * @param modifier Optional modifier for styling
 */
@Composable
fun ContestCard(
    contest: Contest,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Platform Badge and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Platform Badge
                PlatformBadge(platform = contest.site)

                // Status Badge
                StatusBadge(contest = contest)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contest Name
            Text(
                text = contest.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Start Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Start Time",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatStartTime(contest.startTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Duration
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Duration",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Duration: ${contest.getFormattedDuration()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Badge showing the platform name with color coding
 */
@Composable
private fun PlatformBadge(platform: String) {
    val backgroundColor = Color(platform.getPlatformColor())
    val textColor = if (platform.lowercase() == "atcoder") Color.White else Color.White

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = platform,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Badge showing the contest status (e.g., "Starts in 2 hours")
 */
@Composable
private fun StatusBadge(contest: Contest) {
    val statusText = getStatusText(contest.startTime, contest.status)
    val statusColor = when (contest.status) {
        "CODING" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.tertiary
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = statusColor.copy(alpha = 0.1f)
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = statusColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Format the start time to a readable format
 */
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

/**
 * Get status text based on start time
 */
private fun getStatusText(startTime: String, status: String): String {
    if (status == "CODING") {
        return "Live Now"
    }

    return try {
        val instant = Instant.parse(startTime)
        val now = Instant.now()
        val duration = Duration.between(now, instant)

        when {
            duration.isNegative -> "Started"
            duration.toMinutes() < 60 -> "Starts in ${duration.toMinutes()}m"
            duration.toHours() < 24 -> "Starts in ${duration.toHours()}h"
            duration.toDays() < 7 -> "Starts in ${duration.toDays()}d"
            else -> "Upcoming"
        }
    } catch (e: Exception) {
        "Upcoming"
    }
}

/**
 * Extension function to get platform color
 */
private fun String.getPlatformColor(): Long {
    return when (this.lowercase()) {
        "codeforces" -> 0xFF1976D2
        "codechef" -> 0xFF8B4513
        "leetcode" -> 0xFFFFA116
        "atcoder" -> 0xFF000000
        "hackerrank" -> 0xFF00EA64
        "hackerearth" -> 0xFF323754
        else -> 0xFF6200EE
    }
}
