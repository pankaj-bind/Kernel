# Quick Start Guide

## 🎯 What We Just Built

A complete **Competitive Programming Contest Tracker** with:
- ✅ Modern Jetpack Compose UI
- ✅ Clean Architecture (Data → ViewModel → UI)
- ✅ Real-time contest data from Kontests API
- ✅ Material3 design with bottom navigation
- ✅ Ready for expansion (Alarmy & Notes features)

---

## 🏃 Run the App (3 Steps)

### Step 1: Open in Android Studio
```
File → Open → Navigate to:
C:\Users\Pankaj Kumar Bind\AndroidStudioProjects\Kernel
```

### Step 2: Sync Gradle
- Android Studio will show a banner "Gradle files have changed"
- Click **"Sync Now"**
- Wait for dependencies to download (2-5 minutes on first run)

### Step 3: Run
- Click the green ▶️ **Run** button
- Select an emulator or device
- The app will launch showing the CP Contests screen

---

## 📱 What You'll See

### Home Screen
- Bottom Navigation with 3 tabs:
  - 🏆 **CP Contests** (working)
  - ⏰ **Alarmy** (placeholder)
  - 📝 **Notes** (placeholder)

### CP Contests Tab
- List of upcoming contests from multiple platforms
- Each card shows:
  - Platform name with color badge
  - Contest name
  - Start time (in your local timezone)
  - Duration
  - Status ("Starts in 2 hours" or "Live Now")
- **Tap a card** to open the contest in your browser
- **Pull down** to refresh

---

## 🔧 If You Get Errors

### Gradle Sync Fails
1. Close Android Studio
2. Delete these folders:
   ```
   C:\Users\Pankaj Kumar Bind\AndroidStudioProjects\Kernel\.gradle
   C:\Users\Pankaj Kumar Bind\AndroidStudioProjects\Kernel\.idea
   ```
3. Reopen the project

### Build Errors
```powershell
cd "C:\Users\Pankaj Kumar Bind\AndroidStudioProjects\Kernel"
.\gradlew.bat clean build
```

### Still Having Issues?
Check:
- ✅ Android SDK 36 is installed (Settings → SDK Manager)
- ✅ Java 11+ is configured
- ✅ Internet connection (for downloading dependencies)

---

## 🎨 Customization Quick Tips

### Change Theme Colors
Edit: `app/src/main/java/com/example/kernel/ui/theme/Color.kt`

### Modify Contest Card Design
Edit: `app/src/main/java/com/example/kernel/ui/cp/components/ContestCard.kt`

### Add More Platforms
Edit: `app/src/main/java/com/example/kernel/data/repository/ContestRepository.kt`
- The API already returns all platforms
- No code changes needed!

### Change App Name
Edit: `app/src/main/res/values/strings.xml`
```xml
<string name="app_name">Your App Name</string>
```

---

## 📖 Learn More

- **Architecture Details**: See [SETUP_GUIDE.md](SETUP_GUIDE.md)
- **File Locations**: See [FILE_LOCATIONS.md](FILE_LOCATIONS.md)
- **Full README**: See [README.md](README.md)

---

## ✅ Success Checklist

After running the app, you should see:
- [ ] App launches without crashes
- [ ] Bottom navigation with 3 tabs appears
- [ ] CP Contests tab shows a list of contests
- [ ] Tapping a contest opens it in browser
- [ ] Pull-to-refresh works
- [ ] Platform badges are color-coded
- [ ] Times shown in your local timezone

---

## 🚀 Next: Add Alarmy Feature (Phase 2)

When you're ready to continue:
1. We'll create the Alarm database (Room)
2. Build the alarm creation UI
3. Implement puzzle missions
4. Add AlarmManager for triggering alarms
5. Create notification system

---

## 💡 Pro Tips

1. **Test on Real Device**: For best experience with Material3 dynamic colors
2. **Check Logcat**: Use Android Studio's Logcat to see API responses
3. **Network Inspector**: View API calls in Android Studio's Network Profiler
4. **Hot Reload**: Make UI changes and see them instantly (Ctrl+S)

---

## 📞 Common Questions

**Q: Why do I see "Loading..." forever?**
A: Check your internet connection. The app fetches live data from Kontests API.

**Q: Can I run this offline?**
A: Not yet. Phase 2+ will add offline caching with Room database.

**Q: How do I change the API?**
A: Edit `app/src/main/java/com/example/kernel/di/NetworkModule.kt` (BASE_URL)

**Q: Where's the data coming from?**
A: Free public API at https://kontests.net/api/v1/all

---

<div align="center">

**You're all set! 🎉**

Open Android Studio and run the app!

</div>
