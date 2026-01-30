# Kernel - Productivity Utility App

<div align="center">

📱 **A Modern Android Productivity Suite**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-2024.09.03-green.svg)](https://developer.android.com/jetpack/compose)
[![Material3](https://img.shields.io/badge/Material-3-blue.svg)](https://m3.material.io/)

</div>

---

## 🌟 Features

### ✅ Phase 1 (Implemented)
- **Competitive Programming Tracker** 🏆
  - Track upcoming contests from Codeforces, CodeChef, LeetCode, AtCoder, and more
  - Real-time contest information
  - Platform-specific color coding
  - Status indicators (upcoming/live)
  - Direct links to contests

### 🚧 Phase 2 (Planned)
- **Alarmy Clone** ⏰
  - Smart alarm with puzzle missions
  - Math puzzles, memory games, shake to dismiss
  - Prevent oversleeping

### 🚧 Phase 3 (Planned)
- **Quick Notes** 📝
  - Fast note-taking
  - Categories and tags
  - Search functionality

---

## 🏗️ Architecture

This app follows **Clean Architecture** principles with **MVVM** pattern:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Composables, ViewModels, UI State)   │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│          Domain Layer                   │
│     (Use Cases - Future)                │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│           Data Layer                    │
│   (Repository, API, Models)             │
└─────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

### Core
- **Language**: Kotlin 2.0.20
- **UI Framework**: Jetpack Compose
- **Design System**: Material3
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36

### Architecture Components
- **Dependency Injection**: Hilt
- **Navigation**: Navigation Compose
- **Lifecycle**: ViewModel, StateFlow
- **Asynchrony**: Kotlin Coroutines & Flow

### Networking
- **HTTP Client**: Retrofit 2.11.0
- **JSON Parser**: Moshi (with Kotlin codegen)
- **Logging**: OkHttp Logging Interceptor

### Other
- **Image Loading**: Coil for Compose
- **API**: Kontests API (free, no auth required)

---

## 📁 Project Structure

```
app/src/main/java/com/example/kernel/
├── KernelApplication.kt              # Hilt Application
├── MainActivity.kt                    # Entry point
│
├── data/                              # Data Layer
│   ├── model/                         # Data models
│   ├── remote/                        # API services
│   └── repository/                    # Repository pattern
│
├── di/                                # Dependency Injection
│   └── NetworkModule.kt
│
├── navigation/                        # App navigation
│   ├── Screen.kt
│   └── AppNavigation.kt
│
└── ui/                                # Presentation Layer
    ├── theme/                         # Material3 theme
    ├── home/                          # Home with bottom nav
    └── cp/                            # CP Tracker feature
        ├── CompetitiveProgrammingViewModel.kt
        ├── CompetitiveProgrammingScreen.kt
        └── components/
            └── ContestCard.kt
```

See [FILE_LOCATIONS.md](FILE_LOCATIONS.md) for detailed file listing.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11 or higher
- Android SDK 36

### Installation

1. **Clone or open the project**
   ```bash
   cd "C:\Users\Pankaj Kumar Bind\AndroidStudioProjects\Kernel"
   ```

2. **Open in Android Studio**
   - File → Open → Select the Kernel folder
   - Wait for Gradle sync to complete

3. **Run the app**
   - Click the Run button (▶️)
   - Select an emulator or connected device

### Command Line Build
```powershell
.\gradlew.bat assembleDebug
```

---

## 📱 Screenshots

### Home Screen with Bottom Navigation
- Three tabs: CP Contests, Alarmy, Notes
- Material3 design with dynamic colors

### CP Contests Screen
- List of upcoming contests
- Color-coded platform badges
- Time until contest starts
- Contest duration display

---

## 🌐 API Reference

### Kontests API
- **Base URL**: `https://kontests.net/api/`
- **Documentation**: [kontests.net](https://kontests.net/)
- **Free tier**: Unlimited requests
- **No authentication required**

**Example Response**:
```json
[
  {
    "name": "Codeforces Round #XXX",
    "url": "https://codeforces.com/...",
    "start_time": "2024-01-31T14:35:00.000Z",
    "end_time": "2024-01-31T16:35:00.000Z",
    "duration": "7200",
    "site": "CodeForces",
    "in_24_hours": "Yes",
    "status": "BEFORE"
  }
]
```

---

## 🎨 Design Principles

- **Material Design 3**: Latest design guidelines
- **Dynamic Color**: Adapts to system wallpaper (Android 12+)
- **Dark Mode**: Full support
- **Edge-to-Edge**: Modern immersive UI
- **Accessibility**: WCAG compliant

---

## 🧪 Testing

```powershell
# Run unit tests
.\gradlew.bat test

# Run instrumented tests
.\gradlew.bat connectedAndroidTest
```

---

## 📚 Documentation

- [SETUP_GUIDE.md](SETUP_GUIDE.md) - Complete setup and architecture guide
- [FILE_LOCATIONS.md](FILE_LOCATIONS.md) - Quick file reference

---

## 🗺️ Roadmap

### Phase 1: Competitive Programming Tracker ✅
- [x] Setup project with Compose & Hilt
- [x] Implement API integration
- [x] Create contest list UI
- [x] Add navigation structure
- [x] Platform color coding

### Phase 2: Alarmy Clone
- [ ] Alarm creation UI
- [ ] Database integration (Room)
- [ ] Puzzle missions
- [ ] Notification system
- [ ] AlarmManager integration

### Phase 3: Notes
- [ ] Note CRUD operations
- [ ] Room database
- [ ] Search functionality
- [ ] Categories/tags

### Future Enhancements
- [ ] Contest reminders
- [ ] Calendar sync
- [ ] Offline mode
- [ ] Home screen widget
- [ ] Export/import data

---

## 🤝 Contributing

This is a personal learning project, but suggestions are welcome!

---

## 📄 License

See [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

Built with ❤️ using modern Android development practices

---

## 🙏 Acknowledgments

- [Kontests API](https://kontests.net/) for providing free contest data
- [Material Design 3](https://m3.material.io/) for design guidelines
- Android Developer community

---

<div align="center">

**Happy Coding! 🚀**

</div>

