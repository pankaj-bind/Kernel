# File Locations Reference

## All Created Files for Phase 1

### Application & Configuration
```
app/src/main/java/com/example/kernel/
└── KernelApplication.kt
```

### Data Layer
```
app/src/main/java/com/example/kernel/data/
├── model/
│   └── Contest.kt
├── remote/
│   └── ContestApiService.kt
└── repository/
    └── ContestRepository.kt
```

### Dependency Injection
```
app/src/main/java/com/example/kernel/di/
└── NetworkModule.kt
```

### UI Layer - Theme
```
app/src/main/java/com/example/kernel/ui/theme/
├── Color.kt
├── Theme.kt
└── Type.kt
```

### UI Layer - Home
```
app/src/main/java/com/example/kernel/ui/home/
└── HomeScreen.kt
```

### UI Layer - Competitive Programming
```
app/src/main/java/com/example/kernel/ui/cp/
├── CompetitiveProgrammingViewModel.kt
├── CompetitiveProgrammingScreen.kt
└── components/
    └── ContestCard.kt
```

### Navigation
```
app/src/main/java/com/example/kernel/navigation/
├── Screen.kt
└── AppNavigation.kt
```

### Main Activity
```
app/src/main/java/com/example/kernel/
└── MainActivity.kt
```

### Gradle Configuration
```
build.gradle.kts                    (root)
app/build.gradle.kts                (app module)
gradle/libs.versions.toml           (version catalog)
```

### Manifest
```
app/src/main/AndroidManifest.xml
```

---

## Quick Navigation

### To modify the API:
→ `app/src/main/java/com/example/kernel/data/remote/ContestApiService.kt`

### To change contest data structure:
→ `app/src/main/java/com/example/kernel/data/model/Contest.kt`

### To update contest card UI:
→ `app/src/main/java/com/example/kernel/ui/cp/components/ContestCard.kt`

### To modify home navigation:
→ `app/src/main/java/com/example/kernel/ui/home/HomeScreen.kt`

### To add dependencies:
→ `gradle/libs.versions.toml`

### To change theme colors:
→ `app/src/main/java/com/example/kernel/ui/theme/Color.kt`
→ `app/src/main/java/com/example/kernel/ui/theme/Theme.kt`

---

## Total Files Created: 16

All files are properly organized following Clean Architecture and Android best practices.
