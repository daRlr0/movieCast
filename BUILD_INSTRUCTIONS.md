# Build Instructions - Media Explorer

## 🚀 Quick Start

Follow these steps to build and run the app.

## Prerequisites

- ✅ Android Studio (latest version)
- ✅ JDK 8 or higher
- ✅ Android SDK API 34
- ✅ Min SDK API 26

## Step-by-Step Build Process

### 1. ⚙️ Sync Gradle

```
File → Sync Project with Gradle Files
```

**Wait for**: "Gradle sync finished" message

**Expected**: No errors, all dependencies resolved

### 2. 🧹 Clean Project

```
Build → Clean Project
```

**Wait for**: Clean operation to complete

**Expected**: "Clean finished" message

### 3. 🔨 Rebuild Project

```
Build → Rebuild Project
```

**Wait for**: Full rebuild (may take 1-2 minutes)

**Expected**: 
- "Build: BUILD SUCCESSFUL"
- ViewBinding classes generated
- No compilation errors

### 4. ▶️ Run the App

```
Run → Run 'app'
or press Shift+F10
```

**Select**: Emulator or physical device

**Expected**: App launches successfully

## 🔍 Troubleshooting

### ViewBinding Errors

**Problem**: "Cannot resolve symbol 'ActivityMainBinding'"

**Solution**:
1. Check `build.gradle (Module: app)`:
   ```gradle
   android {
       buildFeatures {
           viewBinding true
       }
   }
   ```
2. File → Invalidate Caches / Restart
3. Rebuild Project

### API Key Not Found

**Problem**: "BuildConfig.API_KEY not found"

**Solution**:
1. Verify `local.properties` contains:
   ```properties
   TMDB_API_KEY=b4926b2588991a8ac82e2142e7bf3ecc
   ```
2. Check `build.gradle` has:
   ```gradle
   buildConfigField "String", "API_KEY", "\"${apiKey}\""
   ```
3. Sync Gradle

### Dependencies Failed

**Problem**: Dependency resolution errors

**Solution**:
1. Check internet connection
2. File → Invalidate Caches / Restart
3. Delete `.gradle` folder in project root
4. Sync Gradle again

### Room Schema Error

**Problem**: "Cannot find Room schema"

**Solution**:
1. Clean Project
2. Rebuild Project
3. Room will auto-generate schema

### Manifest Merge Error

**Problem**: "Manifest merger failed"

**Solution**:
1. Check `AndroidManifest.xml` for duplicates
2. Ensure all activities registered
3. Clean and Rebuild

## ✅ Verification Checklist

After successful build, verify:

- [ ] Splash screen appears
- [ ] Popular movies load
- [ ] Search works
- [ ] Filters work
- [ ] Click movie → Details opens
- [ ] Shared element transition smooth
- [ ] Heart icons toggle favorites
- [ ] Genres display on details
- [ ] Cast list shows
- [ ] Comments save
- [ ] Dark mode works
- [ ] Favorites tab accessible
- [ ] Offline favorites work

## 📱 Running on Device

### Emulator Setup

1. AVD Manager → Create Virtual Device
2. Select: Pixel 4 (or similar)
3. API Level: 34 (Android 14)
4. Download system image if needed
5. Finish & Launch

### Physical Device

1. Enable Developer Options:
   - Settings → About Phone
   - Tap "Build Number" 7 times
2. Enable USB Debugging:
   - Developer Options → USB Debugging
3. Connect via USB
4. Allow USB debugging prompt
5. Run app from Android Studio

## 🎨 Testing Features

### Test Splash Screen
1. Close app completely
2. Launch from launcher
3. ✅ See logo for 2 seconds

### Test Filters
1. Tap "Боевик" chip
2. ✅ Only action movies show
3. Tap "2024" chip
4. ✅ Only 2024 movies show

### Test Search
1. Type "Matrix" in search
2. ✅ Results appear
3. Clear search
4. ✅ Popular movies return

### Test Empty State
1. Search "xyzabc123"
2. ✅ "Ничего не найдено" appears

### Test Dark Mode
1. System Settings → Display → Dark Mode
2. Toggle ON
3. ✅ App switches to dark theme
4. Toggle OFF
5. ✅ App switches to light theme

### Test Transitions
1. Click any movie card
2. ✅ Poster smoothly expands
3. ✅ Details fade in
4. Press back
5. ✅ Animates back smoothly

### Test Favorites
1. Click heart on main screen
2. ✅ Heart fills red instantly
3. Go to Details
4. ✅ Heart already filled
5. Go to Favorites tab
6. ✅ Movie appears there
7. Turn off WiFi
8. ✅ Genres & cast still visible

### Test Comments
1. Open movie details
2. Type comment
3. Tap "Сохранить"
4. Exit and reopen
5. ✅ Comment persists

## 📊 Build Variants

### Debug (Default)
- BuildType: debug
- Debuggable: true
- Minify: false

### Release (Production)
- BuildType: release
- Debuggable: false
- Minify: true (if configured)

To build release:
```
Build → Generate Signed Bundle/APK
```

## 🔧 Common Gradle Commands

### Terminal Commands

```bash
# Sync dependencies
./gradlew dependencies

# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on device
./gradlew installDebug

# Run tests
./gradlew test
```

## 📦 Output Locations

### Debug APK
```
app/build/outputs/apk/debug/app-debug.apk
```

### Release APK
```
app/build/outputs/apk/release/app-release.apk
```

## 🐛 Debug Mode

### Enable Logcat
1. Android Studio → Logcat
2. Filter: "MovieCast"
3. See all app logs

### Common Logs
```
MovieRepository: Loading popular movies
MainViewModel: Movies loaded: 20
MovieDetailsActivity: Loading details for ID: 123
Glide: Image loaded successfully
```

## 💡 Performance Tips

### Faster Builds
1. Enable parallel builds:
   ```gradle
   org.gradle.parallel=true
   org.gradle.caching=true
   ```
2. Increase heap size:
   ```gradle
   org.gradle.jvmargs=-Xmx2048m
   ```

### Faster Emulator
1. Use ARM64 system image
2. Enable hardware acceleration
3. Allocate more RAM (2GB+)

## 📝 Pre-Launch Checklist

Before showing to users:

- [ ] All features working
- [ ] No crash on startup
- [ ] API key configured
- [ ] Internet permission in manifest
- [ ] All activities registered
- [ ] Proguard rules (if release)
- [ ] Icon set (optional)
- [ ] Version number updated

## 🎯 Build Success Criteria

You know the build is successful when:

1. ✅ Gradle sync: No errors
2. ✅ Build output: "BUILD SUCCESSFUL"
3. ✅ App installs on device
4. ✅ Splash screen appears
5. ✅ Movies load and display
6. ✅ All navigation works
7. ✅ No runtime crashes

## 🚦 Build Status Indicators

| Status | Meaning | Action |
|--------|---------|--------|
| 🟢 Green | Build success | Run app |
| 🟡 Yellow | Warnings | Check, then run |
| 🔴 Red | Build failed | Fix errors |

## 📞 Support

If you encounter issues:

1. Check error messages in Build tab
2. Review Logcat for runtime errors
3. Verify all prerequisites
4. Try Clean & Rebuild
5. Invalidate Caches & Restart

## 🎉 Success!

Once you see:
- ✅ Splash screen
- ✅ Movie grid
- ✅ Smooth UI

**You're done!** The app is running successfully! 🎬

---

**Build Time**: ~2-3 minutes (first time)  
**App Size**: ~10-15 MB  
**Launch Time**: <3 seconds  

**Ready to enjoy Media Explorer!** 🚀
