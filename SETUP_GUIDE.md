# Media Explorer - Setup Guide

## Quick Start

### Prerequisites
- Android Studio (latest version recommended)
- JDK 8 or higher
- Android SDK with API level 34
- Internet connection for API calls

### Installation Steps

1. **Open Project**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the MovieCast folder
   - Click "OK"

2. **Sync Gradle**
   - Android Studio will automatically prompt to sync Gradle
   - Wait for the sync to complete
   - If any dependencies fail, click "Sync Now" again

3. **Verify API Key**
   - Check that `local.properties` contains:
     ```
     TMDB_API_KEY=b4926b2588991a8ac82e2142e7bf3ecc
     ```
   - This file is already configured

4. **Build Project**
   - Click "Build" → "Make Project" (or Ctrl+F9)
   - Wait for the build to complete
   - Fix any errors if they appear (should be none)

5. **Run Application**
   - Connect an Android device or start an emulator
   - Click the "Run" button (green triangle) or press Shift+F10
   - Select your device/emulator
   - Wait for the app to install and launch

## Troubleshooting

### Build Errors

**Error: BuildConfig.API_KEY not found**
- Solution: Make sure `local.properties` has the API key
- Clean and rebuild: Build → Clean Project, then Build → Rebuild Project

**Error: Room schema export**
- Solution: This is normal if you see a warning about schema export
- The app will work fine without exporting schemas

**Error: ViewBinding not enabled**
- Solution: Already enabled in build.gradle
- Sync Gradle again if needed

### Runtime Issues

**No movies loading**
- Check internet connection
- Verify API key is correct in local.properties
- Check logcat for network errors

**App crashes on launch**
- Check minimum SDK version (must be API 26+)
- Clear app data and try again
- Check logcat for error messages

**Images not loading**
- Internet permission is already added
- Check if Glide is properly included in dependencies
- Verify network connectivity

### Testing on Emulator

**Recommended Emulator Settings:**
- Device: Pixel 4 or similar
- System Image: API 30 or higher
- RAM: 2GB minimum
- Enable: "Use Host GPU"

### Testing Checklist

Before considering the app complete, test:

1. **Main Screen**
   - [ ] Popular movies load on startup
   - [ ] Scroll down to load more pages
   - [ ] Search for a movie (e.g., "Аватар")
   - [ ] Clear search returns to popular movies
   - [ ] Click a movie to view details

2. **Details Screen**
   - [ ] Movie poster and info display correctly
   - [ ] Cast members appear with photos
   - [ ] Toggle favorite button
   - [ ] Add a comment and save
   - [ ] Share button opens share dialog
   - [ ] Trailer button opens YouTube (if available)

3. **Favorites Screen**
   - [ ] Navigate using bottom navigation
   - [ ] Favorited movies appear
   - [ ] Click favorite to view details
   - [ ] Remove from favorites works
   - [ ] Empty state shows when no favorites

4. **Network Handling**
   - [ ] Turn off internet → see error toast
   - [ ] Turn on internet → movies load
   - [ ] Favorites work offline

5. **Data Persistence**
   - [ ] Add favorites and close app
   - [ ] Reopen app and check favorites persist
   - [ ] Comments are saved across sessions

## Project File Checklist

Verify these files exist:

### Core Files
- [x] `app/build.gradle` - Dependencies configured
- [x] `local.properties` - API key stored
- [x] `AndroidManifest.xml` - Permissions and activities registered

### Data Layer
- [x] `MediaItem.java` - Room entity
- [x] `MovieDao.java` - Database operations
- [x] `MovieDatabase.java` - Room database
- [x] `TMDbApiService.java` - API interface
- [x] `RetrofitClient.java` - Network client
- [x] `MovieRepository.java` - Data management

### UI Layer
- [x] `MainActivity.java` - Main screen
- [x] `MovieDetailsActivity.java` - Details screen
- [x] `FavoritesActivity.java` - Favorites screen
- [x] `MainViewModel.java` - Main screen logic
- [x] `MovieDetailsViewModel.java` - Details logic
- [x] `FavoritesViewModel.java` - Favorites logic

### Adapters
- [x] `MovieAdapter.java` - Movies grid
- [x] `CastAdapter.java` - Cast list
- [x] `FavoritesAdapter.java` - Favorites grid

### Layouts
- [x] `activity_main.xml` - Main screen layout
- [x] `activity_movie_details.xml` - Details layout
- [x] `activity_favorites.xml` - Favorites layout
- [x] `item_movie.xml` - Movie card layout
- [x] `item_cast.xml` - Cast member layout

### Utilities
- [x] `PaginationScrollListener.java` - Scroll handling
- [x] `NetworkUtil.java` - Network checking

## API Configuration Details

**Base URL**: https://api.themoviedb.org/3/

**API Key**: b4926b2588991a8ac82e2142e7bf3ecc

**Language**: ru-RU (Russian)

**Image URLs**:
- Posters: https://image.tmdb.org/t/p/w500/{poster_path}
- Profiles: https://image.tmdb.org/t/p/w185/{profile_path}

## Performance Tips

1. **Image Loading**
   - Glide automatically caches images
   - First load may be slower
   - Subsequent loads are fast

2. **Pagination**
   - Loads 20 movies per page
   - Total pages available: ~500
   - Scroll triggers automatic loading

3. **Database**
   - Room provides efficient local storage
   - Favorites load instantly from local DB
   - No internet needed for favorites

## Development Notes

### Code Style
- Java 8 features enabled
- MVVM architecture strictly followed
- ViewBinding used throughout
- LiveData for reactive updates

### Threading
- Network calls on background threads (Retrofit)
- Database operations on background threads (Executor)
- UI updates on main thread (LiveData)

### Error Handling
- Network errors show Toast messages
- Loading states show ProgressBar
- Empty states show informative messages

## Next Steps After Setup

1. Run the app and verify all features work
2. Test with different movies and searches
3. Add several movies to favorites
4. Test offline functionality
5. Share a movie to verify Intent works
6. Open a trailer to verify YouTube integration

## Support

If you encounter issues:
1. Check logcat for detailed error messages
2. Verify all dependencies downloaded correctly
3. Clean and rebuild the project
4. Invalidate caches (File → Invalidate Caches / Restart)
5. Check that minimum SDK version matches your device

## Conclusion

The application is fully implemented with:
- ✅ MVVM Architecture
- ✅ Room Database
- ✅ Retrofit API Integration
- ✅ Pagination
- ✅ Search Functionality
- ✅ Favorites Management
- ✅ User Comments
- ✅ Share & Trailer Features
- ✅ ViewBinding
- ✅ Error Handling
- ✅ Loading States

Ready to build and run!
