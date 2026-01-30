# Kernel - Productivity App Setup Guide

## 📁 Project Structure Overview

Your Android project has been set up with **Clean Architecture** principles and **MVVM** pattern. Here's the complete directory structure:

```
app/src/main/java/com/example/kernel/
├── KernelApplication.kt              # Hilt Application class
├── MainActivity.kt                    # Main Compose Activity
│
├── data/                              # Data Layer
│   ├── model/
│   │   └── Contest.kt                 # Contest data model
│   ├── remote/
│   │   └── ContestApiService.kt       # Retrofit API interface
│   └── repository/
│       └── ContestRepository.kt       # Repository implementation
│
├── di/                                # Dependency Injection
│   └── NetworkModule.kt               # Hilt module for networking
│
├── navigation/                        # Navigation
│   ├── Screen.kt                      # Navigation routes
│   └── AppNavigation.kt               # NavHost setup
│
└── ui/                                # Presentation Layer
    ├── theme/
    │   ├── Color.kt                   # Theme colors
    │   ├── Theme.kt                   # Material3 theme
    │   └── Type.kt                    # Typography
    │
    ├── home/
    │   └── HomeScreen.kt              # Home screen with bottom navigation
    │
    └── cp/                            # Competitive Programming feature
        ├── CompetitiveProgrammingViewModel.kt
        ├── CompetitiveProgrammingScreen.kt
        └── components/
            └── ContestCard.kt         # Reusable contest card UI
```

---

## 🔧 Dependencies Added

### Core Libraries
- **Kotlin**: 2.0.20
- **Jetpack Compose**: BOM 2024.09.03
  - Material3
  - Navigation Compose
  - Activity Compose
  - UI Tooling

### Architecture Components
- **Hilt**: 2.51.1 (Dependency Injection)
- **Lifecycle**: 2.8.6 (ViewModel, Compose integration)
- **Navigation**: 2.8.2

### Networking
- **Retrofit**: 2.11.0
- **OkHttp**: 4.12.0 (with logging interceptor)
- **Moshi**: 1.15.1 (JSON parsing with Kotlin support)

### Other
- **Coil**: 2.7.0 (Image loading for Compose)
- **Coroutines**: 1.8.1

---

## 🌐 API Information

### Kontests API
- **Base URL**: `https://kontests.net/api/`
- **Endpoint**: `GET /v1/all`
- **Description**: Free public API that aggregates upcoming contests from multiple platforms

### Supported Platforms:
- ✅ Codeforces
- ✅ CodeChef
- ✅ LeetCode
- ✅ AtCoder
- ✅ HackerRank
- ✅ HackerEarth

### Response Format:
```json
[
  {
    "name": "Contest Name",
    "url": "https://contest-url.com",
    "start_time": "2024-01-31T10:00:00.000Z",
    "end_time": "2024-01-31T12:00:00.000Z",
    "duration": "7200",
    "site": "CodeForces",
    "in_24_hours": "Yes",
    "status": "BEFORE"
  }
]
```

---

## 📝 Key Files Explained

### 1. **Contest.kt** (Data Model)
```kotlin
@JsonClass(generateAdapter = true)
data class Contest(
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String,
    @Json(name = "start_time") val startTime: String,
    @Json(name = "end_time") val endTime: String,
    @Json(name = "duration") val duration: String,
    @Json(name = "site") val site: String,
    @Json(name = "in_24_hours") val in24Hours: String,
    @Json(name = "status") val status: String
)
```
- **Location**: `app/src/main/java/com/example/kernel/data/model/Contest.kt`
- **Purpose**: Represents a competitive programming contest
- **Features**:
  - Moshi annotations for JSON parsing
  - Helper methods for formatted duration
  - Platform-specific color coding

### 2. **ContestApiService.kt** (Retrofit Interface)
```kotlin
interface ContestApiService {
    @GET("v1/all")
    suspend fun getAllContests(): List<Contest>
}
```
- **Location**: `app/src/main/java/com/example/kernel/data/remote/ContestApiService.kt`
- **Purpose**: Defines API endpoints using Retrofit
- **Note**: Uses `suspend` for coroutine support

### 3. **ContestRepository.kt** (Repository Pattern)
```kotlin
@Singleton
class ContestRepository @Inject constructor(
    private val apiService: ContestApiService
) {
    fun getAllContests(): Flow<Result<List<Contest>>> = flow {
        try {
            val contests = apiService.getAllContests()
            emit(Result.success(contests.filter { it.status == "BEFORE" }))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
```
- **Location**: `app/src/main/java/com/example/kernel/data/repository/ContestRepository.kt`
- **Purpose**: Abstracts data source, provides clean API for ViewModels
- **Features**:
  - Uses Kotlin Flow for reactive data
  - Error handling with Result wrapper
  - Filters upcoming contests

### 4. **CompetitiveProgrammingViewModel.kt**
```kotlin
@HiltViewModel
class CompetitiveProgrammingViewModel @Inject constructor(
    private val repository: ContestRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ContestUiState>(ContestUiState.Loading)
    val uiState: StateFlow<ContestUiState> = _uiState.asStateFlow()
    
    fun loadContests() { /* ... */ }
    fun refresh() { /* ... */ }
}
```
- **Location**: `app/src/main/java/com/example/kernel/ui/cp/CompetitiveProgrammingViewModel.kt`
- **Purpose**: Manages UI state and business logic
- **Features**:
  - Hilt injection
  - StateFlow for reactive UI updates
  - Sealed class for UI states (Loading, Success, Error, Empty)

### 5. **ContestCard.kt** (Reusable UI Component)
```kotlin
@Composable
fun ContestCard(
    contest: Contest,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card { /* ... */ }
}
```
- **Location**: `app/src/main/java/com/example/kernel/ui/cp/components/ContestCard.kt`
- **Purpose**: Displays contest information in a Material3 card
- **Features**:
  - Platform badge with color coding
  - Status badge (e.g., "Starts in 2 hours", "Live Now")
  - Formatted start time in local timezone
  - Duration display
  - Click handling to open contest URL

### 6. **CompetitiveProgrammingScreen.kt**
```kotlin
@Composable
fun CompetitiveProgrammingScreen(
    viewModel: CompetitiveProgrammingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold { /* ... */ }
}
```
- **Location**: `app/src/main/java/com/example/kernel/ui/cp/CompetitiveProgrammingScreen.kt`
- **Purpose**: Main screen for CP contests feature
- **Features**:
  - LazyColumn for scrollable list
  - Loading, Empty, and Error states
  - Pull-to-refresh functionality
  - Opens contest URLs in browser

### 7. **HomeScreen.kt** (Bottom Navigation)
- **Location**: `app/src/main/java/com/example/kernel/ui/home/HomeScreen.kt`
- **Purpose**: Dashboard with bottom navigation
- **Tabs**:
  - 🏆 CP Contests
  - ⏰ Alarmy (placeholder)
  - 📝 Notes (placeholder)

---

## 🚀 How to Run

### Option 1: Sync with Android Studio
1. Open the project in Android Studio
2. Let Gradle sync automatically (or click "Sync Now")
3. Wait for dependencies to download
4. Run the app on an emulator or physical device

### Option 2: Command Line
```powershell
cd "C:\Users\Pankaj Kumar Bind\AndroidStudioProjects\Kernel"
.\gradlew.bat assembleDebug
```

---

## 🎨 UI Features

### Home Screen
- Material3 Bottom Navigation Bar
- Three tabs for different features
- Edge-to-edge design

### CP Contests Screen
- **Top App Bar**: Title + Refresh button
- **Contest Cards**: Each showing:
  - Platform badge (color-coded)
  - Contest name
  - Start time (local timezone)
  - Duration
  - Status ("Starts in X hours" or "Live Now")
- **States**:
  - Loading: Circular progress indicator
  - Empty: Friendly empty state message
  - Error: Error message with retry button
  - Success: Scrollable list of contests

### Theme
- Material3 Dynamic Color (Android 12+)
- Dark mode support
- Custom platform colors for badges

---

## 🔄 Data Flow

```
UI (Composable)
    ↓ collect StateFlow
ViewModel
    ↓ calls
Repository
    ↓ calls
API Service (Retrofit)
    ↓ HTTP request
Kontests API
```

---

## 📋 Next Steps (Phase 2+)

### Alarmy Clone Feature
- Alarm creation & management
- Puzzle missions (math, memory, shake)
- Database (Room) for storing alarms
- AlarmManager integration
- Notification handling

### Notes Feature
- Create/edit/delete notes
- Room database
- Search functionality
- Categories/tags

### Enhancements
- Add platform filtering for CP contests
- Add calendar integration
- Add reminders for contests
- Offline caching
- Widget support

---

## 🐛 Troubleshooting

### Gradle Sync Issues
If you encounter Gradle errors:
```powershell
.\gradlew.bat clean
.\gradlew.bat --refresh-dependencies
```

### Build Errors
- Make sure Android SDK 36 is installed
- Check that Java 11+ is being used
- Clear Gradle cache if needed

### Runtime Issues
- Ensure internet permission is in manifest (already added)
- Check internet connectivity for API calls

---

## 📚 Additional Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Kontests API](https://kontests.net/api)
- [Material3 Design](https://m3.material.io/)

---

## ✅ Checklist

- [x] Dependencies configured
- [x] Hilt setup
- [x] Network module
- [x] Data models
- [x] Repository pattern
- [x] ViewModel with StateFlow
- [x] Navigation setup
- [x] Home screen with bottom nav
- [x] CP Contests screen
- [x] Contest card component
- [x] Theme setup
- [x] API integration
- [ ] Run and test the app
- [ ] Phase 2: Alarmy feature
- [ ] Phase 3: Notes feature

---

## 🎯 Summary

You now have a fully structured Android app with:
- ✅ **Clean Architecture** (Data, Domain, Presentation layers)
- ✅ **MVVM Pattern** (ViewModel + StateFlow)
- ✅ **Jetpack Compose** (Modern declarative UI)
- ✅ **Material3** (Latest design system)
- ✅ **Hilt** (Dependency injection)
- ✅ **Retrofit** (Network calls)
- ✅ **Navigation** (Bottom nav + screen navigation)
- ✅ **CP Tracker** (Fully functional feature)

**Next**: Open in Android Studio, sync Gradle, and run the app!
