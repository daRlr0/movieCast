# Media Explorer - Quick Reference Card

## 🚀 Quick Start
1. Open project in Android Studio
2. Sync Gradle
3. Run app (Shift+F10)

## 🔑 API Configuration
- **API Key**: b4926b2588991a8ac82e2142e7bf3ecc
- **Location**: `local.properties`
- **Access**: `BuildConfig.API_KEY`
- **Language**: ru-RU

## 📱 Main Features

### Main Screen
- Grid of popular movies (2 columns)
- Search movies by title
- Infinite scroll pagination
- Click movie → Details

### Details Screen
- Movie info + poster
- Cast list with photos
- ⭐ Add/Remove favorite
- 💬 Save comment
- 🔗 Share movie
- ▶️ Watch trailer

### Favorites Screen
- All saved movies
- Works offline
- Click → Details

## 📂 Key Files

### Activities
- `MainActivity.java` - Browse movies
- `MovieDetailsActivity.java` - Movie details
- `FavoritesActivity.java` - Saved movies

### ViewModels
- `MainViewModel.java` - Main logic
- `MovieDetailsViewModel.java` - Details logic
- `FavoritesViewModel.java` - Favorites logic

### Database
- `MediaItem.java` - Entity
- `MovieDao.java` - Database operations
- `MovieDatabase.java` - Room DB

### API
- `TMDbApiService.java` - API interface
- `RetrofitClient.java` - HTTP client
- `MovieRepository.java` - Data layer

### Adapters
- `MovieAdapter.java` - Movies grid
- `CastAdapter.java` - Cast list
- `FavoritesAdapter.java` - Favorites grid

## 🛠️ Dependencies

```gradle
// Network
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

// Database
implementation 'androidx.room:room-runtime:2.6.1'

// Images
implementation 'com.github.bumptech.glide:glide:4.16.0'

// Architecture
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.7.0'
implementation 'androidx.lifecycle:lifecycle-livedata:2.7.0'
```

## 🔍 Debugging

### Common Issues
**Movies not loading?**
- Check internet connection
- Verify API key in `local.properties`
- Check logcat for errors

**Build errors?**
- Clean project: Build → Clean
- Rebuild: Build → Rebuild
- Sync Gradle

**App crashes?**
- Check minimum SDK (API 26+)
- Clear app data
- Check logcat

## 📊 API Endpoints

| Endpoint | Purpose |
|----------|---------|
| `/movie/popular` | Popular movies |
| `/search/movie` | Search movies |
| `/movie/{id}` | Movie details |
| `/movie/{id}/credits` | Cast info |
| `/movie/{id}/videos` | Trailers |

## 🎨 Architecture

```
Activity/Fragment
    ↓ (observes)
ViewModel
    ↓ (calls)
Repository
    ↓              ↓
API (Retrofit)   Room DB
```

## 📝 Testing Checklist

- [ ] Load popular movies
- [ ] Search for movies
- [ ] View movie details
- [ ] Add to favorites
- [ ] Save comment
- [ ] Share movie
- [ ] Watch trailer
- [ ] View favorites
- [ ] Remove favorite
- [ ] Test offline mode

## 🔐 Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 📸 Image URLs

- **Posters**: `https://image.tmdb.org/t/p/w500/{poster_path}`
- **Profiles**: `https://image.tmdb.org/t/p/w185/{profile_path}`

## 💾 Database Schema

```sql
TABLE: media_items
- id (INTEGER, PRIMARY KEY)
- title (TEXT)
- overview (TEXT)
- posterPath (TEXT)
- releaseDate (TEXT)
- voteAverage (REAL)
- isFavorite (INTEGER/BOOLEAN)
- userComment (TEXT)
```

## 🎯 Key Classes

| Class | Purpose |
|-------|---------|
| `MediaItem` | Room entity |
| `MovieDao` | DB operations |
| `TMDbApiService` | API calls |
| `MovieRepository` | Data management |
| `MainViewModel` | Business logic |
| `MovieAdapter` | Display movies |

## 🌐 Network

- **Base URL**: `https://api.themoviedb.org/3/`
- **Timeout**: Default (10s)
- **Cache**: Glide handles images
- **Threading**: Background (automatic)

## ⚡ Performance Tips

1. Glide caches images automatically
2. Pagination loads 20 at a time
3. Room provides fast local access
4. LiveData prevents memory leaks
5. ViewBinding faster than findViewById

## 🎓 Code Standards

- ✅ MVVM architecture
- ✅ ViewBinding (no findViewById)
- ✅ LiveData for data
- ✅ Repository pattern
- ✅ Background threading
- ✅ Error handling
- ✅ Null safety

## 📱 Requirements

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34
- **Language**: Java
- **Architecture**: MVVM

## 🔄 Data Flow

1. User action → Activity
2. Activity → ViewModel method
3. ViewModel → Repository
4. Repository → API/Database
5. Data → LiveData
6. LiveData → Observer (Activity)
7. Activity updates UI

## 🎉 Status

✅ All tasks completed
✅ Ready to build
✅ Ready to test
✅ Ready to deploy

---

**Version**: 1.0  
**Last Updated**: 2026-02-13  
**Status**: Production Ready
