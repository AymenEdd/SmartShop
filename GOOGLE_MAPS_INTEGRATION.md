# Google Maps Integration for SmartShop

## Overview

This implementation adds geolocation capture during user registration and displays user locations on Google Maps in the admin dashboard. Users can view their location coordinates and open them in Google Maps.

## Backend Setup

### 1. Environment Configuration

Add your Google Maps API key to `backend/.env`:

```env
GOOGLE_MAPS_API_KEY=YOUR_ACTUAL_GOOGLE_MAPS_API_KEY
```

**Getting your Google Maps API key:**

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable the following APIs:
   - Geocoding API
   - Maps Static API
   - Maps SDK for Android
4. Create an API key (Credentials → Create Credentials → API Key)
5. Copy the key to your `.env` file

### 2. Backend Features

**Maps Service** (`app/services/maps_service.py`):

- `reverse_geocode(latitude, longitude)` — Converts coordinates to address
- `geocode_address(address)` — Converts address to coordinates

**Backend Models** (`app/models/user.py`):

- Added `latitude` and `longitude` fields to User model
- Automatically created via migration in `app/main.py`

**User Registration** (`app/routes/auth.py`):

- Accepts optional `latitude` and `longitude` during registration
- Stores location data with user profile

## Mobile Setup

### 1. Android Configuration

**AndroidManifest.xml:**

- Added Google Maps API key meta-data:
  ```xml
  <meta-data
      android:name="com.google.android.geo.API_KEY"
      android:value="YOUR_GOOGLE_MAPS_API_KEY" />
  ```
- Added location permissions:
  ```xml
  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
  <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
  ```

**build.gradle.kts:**

- Added dependencies:
  ```gradle
  implementation("com.google.maps.android:maps-compose:4.3.3")
  implementation("com.google.android.gms:play-services-maps:18.2.0")
  ```

### 2. UI Components

**RegisterScreen** (`ui/screens/RegisterScreen.kt`):

- Added "Capturer la localisation" button that requests GPS permission
- Displays captured coordinates and address preview
- Integrates `MapLinkButton` to show captured location
- Sends `latitude` and `longitude` with registration payload

**MapLinkButton** (`ui/components/GoogleMap.kt`):

- Displays location coordinates with formatted address
- "Voir sur la carte" button opens:
  - Google Maps app (if installed) with direct location
  - Google Maps web (fallback) if app unavailable
- Used in both RegisterScreen and AdminDashboardScreen

**AdminDashboardScreen** (`ui/screens/AdminDashboardScreen.kt`):

- Users card displays all registered users
- Shows user name, email, and location card
- Each user location has clickable map button
- Navigates to Google Maps showing user's registered location

## Testing

### Backend Testing

```bash
# 1. Start backend server
cd backend
python -m uvicorn app.main:app --reload

# 2. Test registration with location
curl -X POST http://localhost:8000/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "full_name": "Test User",
    "password": "secret123",
    "latitude": 48.8566,
    "longitude": 2.3522
  }'

# 3. Test login (get token)
curl -X POST http://localhost:8000/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "Rais200123"
  }'

# 4. Fetch all users (admin only)
curl -X GET http://localhost:8000/admin/users \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Mobile Testing

1. **Build and run the app:**

   ```bash
   cd mobile
   ./gradlew build
   # Or deploy to emulator/device
   ```

2. **Test registration flow:**
   - Open app → "Create account"
   - Fill name, email, password
   - Click "Capturer la localisation"
   - Grant location permission when prompted
   - Observe coordinates displayed
   - Click "Voir sur la carte" to open Google Maps
   - Complete registration

3. **Test admin dashboard:**
   - Login as admin (admin@example.com / Rais200123)
   - Navigate to Admin Dashboard
   - Scroll to "Users" section
   - Click "Voir sur la carte" for any user with location
   - Verify Google Maps opens with correct coordinates

## Architecture

```
Backend:
  app/services/maps_service.py          ← Google Maps API integration
  app/routes/auth.py                    ← Registration with location
  app/models/user.py                    ← User model with lat/long
  app/core/config.py                    ← API key configuration

Mobile:
  ui/components/GoogleMap.kt            ← Reusable map button component
  ui/screens/RegisterScreen.kt          ← Location capture UI
  ui/screens/AdminDashboardScreen.kt    ← User list with map links
  AndroidManifest.xml                   ← Permissions & API key
```

## Security Notes

- **Never commit API keys** — Use environment variables only
- Google Maps API keys should have **API restrictions** set to Android only
- Enable only the APIs you need (Geocoding, Maps SDK for Android)
- Consider setting Application restrictions to your app package name

## Troubleshooting

### GPS Permission Denied

- Grant location permission from device settings → Apps → SmartShop → Permissions → Location
- On emulator, use location spoofing tools

### Google Maps Not Opening

- Verify Google Maps is installed on device
- Check API key is correctly set in AndroidManifest.xml
- Verify location coordinates are valid

### Backend Maps Errors

- Confirm `GOOGLE_MAPS_API_KEY` is set in `.env`
- Check API is enabled in Google Cloud Console
- Verify API quota hasn't been exceeded

## Future Enhancements

- Display all users on a live map view
- Reverse geocoding to show address in registration
- Location history and movement tracking
- Geofencing for store locations
- Distance calculation between users and stores
