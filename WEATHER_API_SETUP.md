# Weather API Setup Guide

## Overview
The AuraFit app now includes a weather indicator that shows current weather conditions (sunny/rainy) to help users choose appropriate outfits.

## Current Status: TESTING MODE
**The weather service randomly shows "Sunny" or "Rainy" weather for testing purposes.**

## Setup Instructions

### For Testing (Current Mode)
- Weather indicator randomly shows "Sunny" (70% chance) or "Rainy" (30% chance)
- Sunny: 25°C with sun icon, Rainy: 18°C with rain icon
- No API key or internet connection required
- Perfect for UI testing and development

### For Production (Real Weather Data)
1. Get OpenWeatherMap API Key
   - Go to [OpenWeatherMap](https://openweathermap.org/api)
   - Sign up for a free account
   - Get your API key from the dashboard

2. Update API Key
   - Open `aurafit-app/app/src/main/java/com/aurafit/AuraFitApp/ui/generate/WeatherService.java`
   - Replace `YOUR_OPENWEATHER_API_KEY` with your actual API key:
     ```java
     private static final String API_KEY = "your_actual_api_key_here";
     ```

3. Enable Real API
   - Uncomment the real API implementation in `getCurrentWeather()` method
   - Comment out the testing implementation

### 4. Add Internet Permission
Make sure your `AndroidManifest.xml` includes internet permission:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

### 5. Features
- **Weather Indicator**: Shows current weather condition (sunny/rainy) in the top-right corner
- **Temperature Display**: Shows current temperature in Celsius
- **Gender-Aware Recommendations**: Provides personalized outfit suggestions based on user's gender preference
- **Location-based**: Uses device location to get local weather (when enabled)
- **Fallback**: Shows default sunny weather if location/API is unavailable
- **Real-time**: Updates weather data when app starts
- **User Data Integration**: Fetches user's clothing preference (Male/Female/Mixed) from Firestore

### 6. Weather Conditions Supported
- **Sunny/Clear**: Shows sun icon with gold color
- **Rainy/Storm/Cloudy**: Shows rain icon with blue color (all non-sunny conditions treated as rainy)
- **Default**: Falls back to sunny weather

### 7. UI Components
- Weather icon (sun/rain)
- Weather condition text
- Temperature display
- Color-coded based on weather type
- Personalized outfit recommendations in instruction text

### 8. Gender-Based Recommendations
The weather service now provides personalized outfit suggestions based on user's clothing preference with only 2 weather options:

**For Male Users:**
- **Sunny**: "Great day for casual shirts and shorts!"
- **Rainy**: "Time for jackets and waterproof gear!"

**For Female Users:**
- **Sunny**: "Perfect for dresses and light tops!"
- **Rainy**: "Layering with cardigans and boots recommended!"

**For Mixed/Unknown:**
- **Sunny**: "Ideal weather for light, comfortable clothing!"
- **Rainy**: "Waterproof and layered clothing suggested!"

**Testing Mode**: Randomly alternates between sunny (70% chance) and rainy (30% chance) weather

The weather indicator helps users make better outfit choices based on current weather conditions and their personal style preferences!
