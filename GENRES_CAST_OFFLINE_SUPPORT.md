# Genres, Cast & Offline Support Implementation

## Overview
Implemented comprehensive content features including genres display, cast list, and full offline support for favorites with all their metadata.

## Features Implemented

### 1. ✅ Genres Display
- Fetches genre mapping from TMDB API (`/genre/movie/list`)
- Displays genre names in Russian (e.g., "Боевик, Драма")
- Shows genres on Details Screen
- Caches genres in Room database for offline use

### 2. ✅ Cast List
- Already implemented RecyclerView with horizontal scroll
- Displays actor photos and names
- Shows character names
- Uses `/movie/{id}/credits` endpoint
- Limited to 10 cast members for better UX

### 3. ✅ Offline Support
- Genres and cast stored in Room database as JSON
- Favorites show full information without internet
- Seamless offline/online experience
- Data persists across app restarts

## Implementation Details

### New API Models

#### Genre.java
```java
public class Genre {
    private int id;
    private String name;
}
```

#### GenreResponse.java
```java
public class GenreResponse {
    private List<Genre> genres;
}
```

### Updated Models

#### Movie.java
Added fields:
```java
@SerializedName("genre_ids")
private List<Integer> genreIds;  // For list endpoints

@SerializedName("genres")
private List<Genre> genres;      // For details endpoint
```

### Database Schema Updates

#### GenreEntity.java (New Table)
```java
@Entity(tableName = "genres")
public class GenreEntity {
    @PrimaryKey
    private int id;
    private String name;
}
```

#### MediaItem.java (Updated)
Added fields for offline support:
```java
private String genresJson;  // JSON array of Genre objects
private String castJson;    // JSON array of Cast objects
```

**Database Version**: Upgraded from 1 to 2

### DAO Updates

#### GenreDao.java (New)
```java
@Dao
public interface GenreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GenreEntity> genres);
    
    @Query("SELECT * FROM genres")
    List<GenreEntity> getAllGenres();
    
    @Query("SELECT COUNT(*) FROM genres")
    int getGenreCount();
}
```

#### MovieDatabase.java (Updated)
- Added GenreEntity to entities
- Added genreDao() method
- Incremented version to 2

### Repository Enhancements

#### MovieRepository.java

**New Methods:**
```java
// Genre API call
public Call<GenreResponse> getGenres()

// Cache genres in database
public void cacheGenres(List<Genre> genres)

// Load genres from cache
private void loadGenresFromCache()

// Get genre names from IDs
public String getGenreNamesFromIds(List<Integer> genreIds)

// Convert with offline data
public MediaItem convertMovieToMediaItemWithExtras(
    Movie movie, boolean isFavorite, String userComment, 
    List<Genre> genres, List<Cast> cast)

// Parse JSON to objects
public List<Genre> getGenresFromJson(String genresJson)
public List<Cast> getCastFromJson(String castJson)
```

**JSON Serialization:**
- Uses Gson to serialize genres and cast to JSON strings
- Stores in Room database for offline access
- Deserializes when loading from database

### ViewModel Updates

#### MovieDetailsViewModel.java

**New Features:**
```java
private MutableLiveData<String> genresLiveData;
private List<Cast> currentCast;
private List<Genre> currentGenres;
```

**Genre Loading:**
```java
private void loadGenres() {
    // Check if genres cached
    if (genreCount == 0) {
        // Fetch from API and cache
        repository.getGenres().enqueue(...)
    }
}
```

**Enhanced loadMovieDetails():**
1. Check database for cached genres/cast (offline support)
2. Fetch from API (online updates)
3. Store genres and cast in memory
4. Update LiveData with genre names

**Save with Offline Data:**
```java
public void toggleFavorite() {
    // Save with genres and cast
    MediaItem newItem = repository.convertMovieToMediaItemWithExtras(
        currentMovie, true, "", currentGenres, currentCast);
}
```

## UI Updates

### activity_movie_details.xml
Added genres TextView:
```xml
<TextView
    android:id="@+id/genresTextView"
    android:textSize="14sp"
    android:textStyle="italic"
    android:textColor="@android:color/holo_blue_dark" />
```

### MovieDetailsActivity.java
Added genre observer:
```java
viewModel.getGenres().observe(this, genres -> {
    if (genres != null && !genres.isEmpty()) {
        binding.genresTextView.setText(genres);
    }
});
```

## Data Flow

### 1. Genre Fetching & Caching

```
App Launch
    ↓
MovieDetailsViewModel created
    ↓
loadGenres() checks cache
    ↓
If empty → Fetch from API (/genre/movie/list)
    ↓
Repository.cacheGenres()
    ↓
Store in genres table
    ↓
Load into genreMap (HashMap)
    ↓
Ready for instant lookups
```

### 2. Movie Details with Genres

```
User opens movie
    ↓
loadMovieDetails(movieId)
    ↓
Check database for cached data
    ↓
Load genresJson & castJson from MediaItem
    ↓
Display cached data (offline support)
    ↓
Fetch from API (if online)
    ↓
movie.getGenres() or movie.getGenreIds()
    ↓
Convert to genre names
    ↓
Update UI
    ↓
Store in currentGenres/currentCast
```

### 3. Saving to Favorites with Offline Data

```
User adds to favorites
    ↓
toggleFavorite() or updateComment()
    ↓
Create MediaItem with extras
    ↓
repository.convertMovieToMediaItemWithExtras(
    movie, isFavorite, comment, 
    currentGenres,  // Live data
    currentCast     // Live data
)
    ↓
Gson serializes to JSON strings
    ↓
Store in media_items table
    ↓
Data saved for offline access
```

### 4. Offline Viewing

```
User opens favorite (no internet)
    ↓
loadMovieDetails(movieId)
    ↓
Check database
    ↓
MediaItem found with genresJson & castJson
    ↓
Parse JSON to objects
    ↓
Display genres and cast
    ↓
Show "Нет подключения к интернету" for API calls
    ↓
All cached data visible offline ✅
```

## Genre Display Format

### Example Output
```
"Боевик, Драма, Триллер"
```

### Logic
```java
private String getGenreNames(List<Genre> genres) {
    StringBuilder names = new StringBuilder();
    for (int i = 0; i < genres.size(); i++) {
        names.append(genres.get(i).getName());
        if (i < genres.size() - 1) {
            names.append(", ");
        }
    }
    return names.toString();
}
```

## Cast Display

### Already Implemented
- Horizontal RecyclerView
- CastAdapter with ViewHolder
- Actor photo (w185 size)
- Actor name
- Character name
- Limited to 10 cast members

### Layout
```
┌─────────────────────────────────────┐
│ Актёры                             │
│ ┌────┐ ┌────┐ ┌────┐ ┌────┐       │
│ │    │ │    │ │    │ │    │  →    │
│ │ 👤 │ │ 👤 │ │ 👤 │ │ 👤 │       │
│ └────┘ └────┘ └────┘ └────┘       │
│ Name   Name   Name   Name          │
│ Role   Role   Role   Role          │
└─────────────────────────────────────┘
```

## Offline Support Details

### What's Stored Offline

| Data | Storage | Format |
|------|---------|--------|
| Movie Info | MediaItem | Native fields |
| Genres | genresJson | JSON string |
| Cast | castJson | JSON string |
| User Comment | userComment | String |
| Favorite Status | isFavorite | Boolean |

### JSON Format Examples

**Genres JSON:**
```json
[
  {"id": 28, "name": "Боевик"},
  {"id": 18, "name": "Драма"}
]
```

**Cast JSON:**
```json
[
  {
    "id": 123,
    "name": "Actor Name",
    "character": "Character Name",
    "profile_path": "/path.jpg"
  }
]
```

### Benefits
1. **Complete offline experience** - All data visible without internet
2. **Fast loading** - No API calls for cached favorites
3. **Data persistence** - Survives app restarts
4. **Seamless UX** - No difference between online/offline for favorites

## API Endpoints Used

### Genre Endpoint
```
GET /genre/movie/list
Parameters:
  - api_key: YOUR_KEY
  - language: ru-RU
Response: GenreResponse with List<Genre>
```

### Movie Details Endpoint (with genres)
```
GET /movie/{id}
Parameters:
  - api_key: YOUR_KEY
  - language: ru-RU
Response: Movie with genres field
```

### Credits Endpoint
```
GET /movie/{id}/credits
Parameters:
  - api_key: YOUR_KEY
  - language: ru-RU
Response: CreditsResponse with List<Cast>
```

## Performance Optimizations

### 1. Genre Map Caching
```java
private Map<Integer, String> genreMap;
```
- O(1) lookup time
- Loaded once on app start
- No repeated database queries

### 2. Lazy Loading
- Genres fetched only if cache empty
- API called once, then cached forever
- Background thread for database operations

### 3. JSON Storage
- Compact storage format
- Fast serialization/deserialization
- Single database query for all data

## Testing Checklist

### Genre Display
- [x] Genres displayed on Details Screen
- [x] Genre names in Russian
- [x] Comma-separated format
- [x] Handles missing genres gracefully

### Cast Display
- [x] Cast list shows horizontally
- [x] Photos load correctly
- [x] Names and characters displayed
- [x] Limited to 10 cast members
- [x] Handles missing photos

### Offline Support
- [x] Add favorite with internet → Save genres & cast
- [x] Turn off internet
- [x] Open favorite → Genres visible
- [x] Open favorite → Cast visible
- [x] All movie data visible offline
- [x] Restart app → Data persists

### Database Migration
- [x] Version 1 to 2 migration successful
- [x] fallbackToDestructiveMigration works
- [x] New fields added to MediaItem
- [x] GenreEntity table created

## Error Handling

### No Internet
```
- Show cached data from database
- Display "Нет подключения к интернету" for missing data
- All favorited content works offline
```

### Missing Genres
```
- Empty string returned
- UI handles gracefully
- No crashes
```

### Missing Cast
```
- Empty list displayed
- No cast section shown
- No crashes
```

## Files Modified/Created

### Created (4 files)
1. ✅ `Genre.java` - Genre model
2. ✅ `GenreResponse.java` - API response
3. ✅ `GenreEntity.java` - Room entity
4. ✅ `GenreDao.java` - Database access

### Modified (9 files)
1. ✅ `Movie.java` - Added genre fields
2. ✅ `MediaItem.java` - Added genresJson & castJson
3. ✅ `MovieDatabase.java` - Added GenreEntity & dao
4. ✅ `TMDbApiService.java` - Added genre endpoint
5. ✅ `MovieRepository.java` - Added genre methods
6. ✅ `MovieDetailsViewModel.java` - Added genre logic
7. ✅ `activity_movie_details.xml` - Added genres TextView
8. ✅ `MovieDetailsActivity.java` - Added genre observer
9. ✅ `app/build.gradle` - (No changes needed, GSON already included)

## Architecture Compliance

### MVVM Pattern ✅
- **Model**: Genre, GenreEntity, MediaItem with JSON
- **View**: MovieDetailsActivity, layout updates
- **ViewModel**: MovieDetailsViewModel handles business logic

### Room Database ✅
- Two tables: media_items, genres
- Proper migration handling
- Background thread operations

### Repository Pattern ✅
- Single source of truth
- Handles API and database
- JSON serialization/deserialization

## Future Enhancements

### Potential Improvements
1. **Genre filtering**: Filter movies by genre on main screen
2. **Cast details**: Click cast member to see bio
3. **Related movies**: "Movies with this actor"
4. **Genre icons**: Visual icons for genres
5. **Smart caching**: Cache expiration after X days

## Conclusion

Successfully implemented:
- ✅ **Genres**: Fetch, cache, and display in Russian
- ✅ **Cast**: Display with photos and names (already implemented)
- ✅ **Offline Support**: Full data persistence for favorites

**Result**: A complete, professional movie app with full offline support for all favorite movies including genres and cast information! 🎬
