# Hybrid Contest System - Implementation Summary

## Overview
This implementation replaces the simple Kontests API aggregator with a **Hybrid Approach**:
1. **Codeforces**: Real API with user rating-based filtering
2. **CodeChef & LeetCode**: Algorithmic local generation based on fixed schedules
3. **Notifications**: WorkManager-based alerts 1 hour before contests

---

## Architecture

### Data Flow
```
┌─────────────────────────────────────────────────────────────┐
│                     CPViewModel                             │
│                         │                                   │
│          ┌──────────────┼──────────────┐                   │
│          ▼              ▼              ▼                   │
│   ContestRepository    ContestNotificationScheduler        │
│          │                                                  │
│    ┌─────┴─────┐                                           │
│    ▼           ▼                                           │
│ CodeforcesAPI  ContestGenerator                            │
│ (Remote)       (Local Algorithm)                           │
└─────────────────────────────────────────────────────────────┘
```

---

## Files Created/Modified

### New Files

#### 1. `data/model/codeforces/CodeforcesModels.kt`
- `CodeforcesResponse<T>` - API response wrapper
- `CodeforcesUser` - User info with rating
- `CodeforcesContest` - Contest from CF API with rating relevance filter

#### 2. `data/remote/CodeforcesApiService.kt`
- Retrofit interface for Codeforces API
- `getUserInfo(handles)` - Get user rating
- `getContestList(gym)` - Get all contests

#### 3. `data/generator/ContestGenerator.kt`
- Algorithmic contest generation for CodeChef & LeetCode
- Seed data based calculations for contest numbers
- Handles weekly/biweekly schedules

#### 4. `worker/ContestNotificationWorker.kt`
- Hilt-enabled WorkManager worker
- Creates notification channel
- Shows notification with contest details

#### 5. `worker/ContestNotificationScheduler.kt`
- Schedules notifications 1 hour before contests
- Uses WorkManager with unique work names
- Supports cancellation and count queries

### Modified Files

#### 6. `data/model/Contest.kt`
- Now uses `ZonedDateTime` for time handling
- `Platform` enum with colors
- `ContestStatus` enum (UPCOMING, LIVE, FINISHED)
- Helper methods: `getFormattedStartTime()`, `getTimeUntilStart()`, `isLive()`

#### 7. `data/repository/ContestRepository.kt`
- Hybrid fetching: Codeforces API + Local generation
- User rating caching
- Rating-based contest filtering
- Platform-specific filtering

#### 8. `ui/cp/CPViewModel.kt`
- Integrates notification scheduler
- Platform filtering support
- Handle update capability

#### 9. `ui/cp/ContestAdapter.kt`
- Updated for new Contest model
- Uses Platform enum for colors

#### 10. `di/NetworkModule.kt`
- Single Retrofit instance for Codeforces
- Removed old Kontests API

#### 11. `AndroidManifest.xml`
- POST_NOTIFICATIONS permission
- VIBRATE permission
- WorkManager provider config

#### 12. `gradle/libs.versions.toml`
- Added WorkManager 2.9.1
- Added Hilt Work 1.2.0

#### 13. `app/build.gradle.kts`
- minSdk raised to 26 (for java.time)
- WorkManager dependencies added

---

## Codeforces Filtering Logic

### For users with rating >= 1900:
- Div. 1 rounds
- Div. 1 + Div. 2 combined
- Global rounds

### For users with rating < 1900:
- Div. 2 rounds
- Div. 3 rounds
- Div. 4 rounds
- Educational rounds

---

## Contest Generation Algorithm

### CodeChef Starters
- **Schedule**: Every Wednesday at 8:00 PM IST
- **Seed**: Starters 224 on Feb 4, 2026
- **Algorithm**: `contestNumber = seedNumber + weeksSince(seedDate)`

### LeetCode Weekly
- **Schedule**: Every Sunday at 8:00 AM IST
- **Seed**: Weekly Contest 487 on Feb 2, 2026
- **Algorithm**: `contestNumber = seedNumber + weeksSince(seedDate)`

### LeetCode Biweekly
- **Schedule**: Every alternate Saturday at 8:00 PM IST
- **Seed**: Biweekly Contest 175 on Feb 1, 2026
- **Algorithm**: `contestNumber = seedNumber + (daysSince(seedDate) / 14)`

---

## Notifications

### Trigger
- Scheduled 1 hour before contest start time
- Uses WorkManager for reliability

### Content
```
Title: 🏆 Contest Starting Soon!
Body: [Contest Name] starts in 1 hour on [Platform]!
```

### Features
- Unique work per contest (no duplicates)
- Auto-cancel on tap
- Vibration enabled
- High priority

---

## API Endpoints Used

### Codeforces
- `GET /api/user.info?handles={handle}` - User rating
- `GET /api/contest.list?gym=false` - All contests

---

## Testing Checklist

- [ ] Codeforces contests load correctly
- [ ] User rating is fetched and cached
- [ ] Contest filtering works by rating
- [ ] CodeChef Starters numbers are correct
- [ ] LeetCode Weekly numbers are correct
- [ ] LeetCode Biweekly numbers are correct
- [ ] Notifications scheduled correctly
- [ ] Notifications fire 1 hour before
- [ ] Tap notification opens app
- [ ] Pull-to-refresh works
- [ ] Platform filtering works

---

## Future Improvements

1. **Settings Screen**: Allow users to:
   - Change Codeforces handle
   - Toggle platforms on/off
   - Enable/disable notifications

2. **Offline Caching**: Room database for offline access

3. **More Platforms**: Add AtCoder, HackerRank if APIs available

4. **Calendar Integration**: Add contests to device calendar

5. **Widget**: Home screen widget showing next contest
