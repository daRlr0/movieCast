# Media Explorer - Android Movie Application

A complete Android application built with Java and MVVM architecture that allows users to browse, search, and manage their favorite movies using The Movie Database (TMDb) API.

## Features

### 1. Movie Browsing
- Display popular movies in a grid layout (2 columns)
- Infinite scroll with pagination support
- Movie cards showing poster, title, release date, and rating

### 2. Search Functionality
- Real-time movie search using SearchView
- Search results with pagination
- Clear search to return to popular movies

### 3. Movie Details
- Comprehensive movie information including:
  - High-resolution poster
  - Title, release date, and rating
  - Full synopsis/overview
  - Cast list with photos (up to 10 actors)
- Interactive features:
  - Add/remove from favorites
  - Add and save personal comments
  - Share movie via Intent
  - Watch trailer on YouTube

### 4. Favorites Management
- Local storage using Room database
- View all favorite movies offline
- Persistent user comments
- Easy removal from favorites

## Technical Stack

### Architecture
- **Pattern**: MVVM (Model-View-ViewModel)
- **Language**: Java
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34

### Libraries & Dependencies
- **Retrofit 2.9.0**: REST API client
- **GSON 2.10.1**: JSON serialization/deserialization
- **Room 2.6.1**: Local database (SQLite wrapper)
- **Glide 4.16.0**: Image loading and caching
- **Lifecycle Components 2.7.0**: ViewModel and LiveData
- **Material Components**: Modern UI components
- **RecyclerView & CardView**: List displays

## Project Structure

```
com.example.moviecast/
├── data/
│   ├── local/
│   │   ├── MediaItem.java          # Room Entity
│   │   ├── MovieDao.java           # Database Access Object
│   │   └── MovieDatabase.java     # Room Database
│   ├── remote/
│   │   ├── model/
│   │   │   ├── Movie.java          # API Movie model
│   │   │   ├── MovieResponse.java  # Paginated response
│   │   │   ├── Cast.java           # Cast member model
│   │   │   ├── CreditsResponse.java
│   │   │   ├── Video.java          # Trailer model
│   │   │   └── VideosResponse.java
│   │   ├── TMDbApiService.java     # Retrofit API interface
│   │   └── RetrofitClient.java     # Retrofit configuration
│   └── repository/
│       └── MovieRepository.java    # Data layer orchestration
├── ui/
│   ├── adapter/
│   │   ├── MovieAdapter.java       # Main screen adapter
│   │   ├── CastAdapter.java        # Cast list adapter
│   │   └── FavoritesAdapter.java   # Favorites adapter
│   ├── viewmodel/
│   │   ├── MainViewModel.java      # Main screen logic
│   │   ├── MovieDetailsViewModel.java
│   │   └── FavoritesViewModel.java
│   └── utils/
│       ├── PaginationScrollListener.java
│       └── NetworkUtil.java        # Network connectivity check
├── MainActivity.java               # Main screen
├── MovieDetailsActivity.java      # Details screen
└── FavoritesActivity.java         # Favorites screen
```

## Setup Instructions

### 1. API Configuration
The TMDb API key is already configured in `local.properties`:
```
TMDB_API_KEY=b4926b2588991a8ac82e2142e7bf3ecc
```

### 2. Build Configuration
The project automatically reads the API key from `local.properties` and injects it into `BuildConfig.API_KEY`.

### 3. Run the Application
1. Open the project in Android Studio
2. Sync Gradle files
3. Run the app on an emulator or physical device

## API Details

- **Base URL**: `https://api.themoviedb.org/3/`
- **Language**: Russian (`ru-RU`)
- **Image Base URL**: `https://image.tmdb.org/t/p/w500/`

### Endpoints Used
- `/movie/popular` - Get popular movies
- `/search/movie` - Search movies
- `/movie/{id}` - Get movie details
- `/movie/{id}/credits` - Get cast information
- `/movie/{id}/videos` - Get trailers

## Key Features Implementation

### ViewBinding
All activities use ViewBinding for type-safe view access:
- `ActivityMainBinding`
- `ActivityMovieDetailsBinding`
- `ActivityFavoritesBinding`

### Pagination
Automatic loading of next pages when scrolling:
- Detects when user reaches bottom of list
- Loads next page seamlessly
- Maintains scroll position

### Offline Support
- Room database stores favorite movies locally
- User comments persist across app sessions
- Favorites accessible without internet

### Error Handling
- Network connectivity checks
- Toast messages for user feedback
- Graceful handling of API failures
- ProgressBar indicators during loading

## UI Components

### Bottom Navigation
Two tabs:
- **Фильмы** (Movies): Browse and search
- **Избранное** (Favorites): View saved movies

### Main Screen
- Toolbar with app title
- SearchView for movie search
- RecyclerView with GridLayoutManager (2 columns)
- Bottom navigation

### Details Screen
- Full-screen poster image
- Movie information section
- Horizontal cast list
- Favorite toggle button
- Comment input with save button
- Share and Trailer buttons

### Favorites Screen
- Grid layout of favorite movies
- Empty state message when no favorites
- Bottom navigation

## Database Schema

### MediaItem Table
```sql
CREATE TABLE media_items (
    id INTEGER PRIMARY KEY,
    title TEXT,
    overview TEXT,
    posterPath TEXT,
    releaseDate TEXT,
    voteAverage REAL,
    isFavorite INTEGER,
    userComment TEXT
)
```

## Permissions Required

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Testing Checklist

- [x] Load popular movies with pagination
- [x] Search movies by title
- [x] View movie details with cast
- [x] Add/remove favorites
- [x] Save and update user comments
- [x] Share movie information
- [x] Open trailers in YouTube
- [x] Navigate between screens
- [x] Handle no internet connection
- [x] Persist data across app restarts

## Future Enhancements

Potential improvements for future versions:
- Movie filtering (by genre, year, rating)
- User ratings and reviews
- Watchlist feature separate from favorites
- Dark theme support
- Advanced search filters
- Movie recommendations
- TV shows support

## License

This project is created for educational purposes as part of a technical assignment.

## Credits

- Movie data provided by [The Movie Database (TMDb)](https://www.themoviedb.org/)
- All movie posters, images, and information are property of their respective owners
