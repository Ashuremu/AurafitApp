package com.aurafit.AuraFitApp.ui.generate;

import android.content.Context;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherService {
    private static final String TAG = "WeatherService";
    private static final String API_KEY = "b5442d6680180a19d17afc0efe3e018d";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    
    public interface WeatherCallback {
        void onWeatherReceived(WeatherData weatherData);
        void onError(String error);
    }
    
    public static class WeatherData {
        public String condition;
        public String description;
        public double temperature;
        public int weatherIcon;
        public int weatherColor;
        public String userGender;
        public String outfitRecommendation;
        
        public WeatherData(String condition, String description, double temperature, int weatherIcon, int weatherColor) {
            this.condition = condition;
            this.description = description;
            this.temperature = temperature;
            this.weatherIcon = weatherIcon;
            this.weatherColor = weatherColor;
            this.userGender = "Unknown";
            this.outfitRecommendation = "";
        }
        
        public WeatherData(String condition, String description, double temperature, int weatherIcon, int weatherColor, String userGender) {
            this.condition = condition;
            this.description = description;
            this.temperature = temperature;
            this.weatherIcon = weatherIcon;
            this.weatherColor = weatherColor;
            this.userGender = userGender;
            this.outfitRecommendation = generateOutfitRecommendation(condition, userGender);
        }
        
        private static String generateOutfitRecommendation(String condition, String gender) {
            if (gender == null || gender.equals("Unknown")) {
                return "Perfect weather for outfit generation!";
            }
            
            if (condition.toLowerCase().equals("forecasting")) {
                if (gender.equals("Male")) {
                    return "Weather forecast available - check for outfit recommendations!";
                } else if (gender.equals("Female")) {
                    return "Weather forecast available - check for outfit recommendations!";
                } else {
                    return "Weather forecast available - check for outfit recommendations!";
                }
            }
            else if (condition.toLowerCase().equals("sunny") || condition.toLowerCase().equals("clear")) {
                if (gender.equals("Male")) {
                    return "Great day for casual shirts and shorts!";
                } else if (gender.equals("Female")) {
                    return "Perfect for dresses and light tops!";
                } else {
                    return "Ideal weather for light, comfortable clothing!";
                }
            } else {
                if (gender.equals("Male")) {
                    return "Time for jackets and waterproof gear!";
                } else if (gender.equals("Female")) {
                    return "Layering with cardigans and boots recommended!";
                } else {
                    return "Waterproof and layered clothing suggested!";
                }
            }
        }
    }
    
    public static void getCurrentWeather(Context context, double latitude, double longitude, WeatherCallback callback) {
        new Thread(() -> {
            try {
                String urlString = BASE_URL + "?lat=" + latitude + "&lon=" + longitude + 
                                 "&appid=" + API_KEY + "&units=metric";
                
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    WeatherData weatherData = parseWeatherData(jsonResponse);
                    
                    ((android.app.Activity) context).runOnUiThread(() -> callback.onWeatherReceived(weatherData));
                    
                } else {
                    ((android.app.Activity) context).runOnUiThread(() -> 
                        callback.onError("Failed to fetch weather data. Response code: " + responseCode));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching weather data", e);
                ((android.app.Activity) context).runOnUiThread(() -> 
                    callback.onError("Error: " + e.getMessage()));
            }
        }).start();
    }
    
    public static void getCurrentWeatherWithUserGender(Context context, double latitude, double longitude, String userGender, WeatherCallback callback) {
        new Thread(() -> {
            try {
                String urlString = BASE_URL + "?lat=" + latitude + "&lon=" + longitude + 
                                 "&appid=" + API_KEY + "&units=metric";
                
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    WeatherData weatherData = parseWeatherDataWithGender(jsonResponse, userGender);
                    
                    ((android.app.Activity) context).runOnUiThread(() -> callback.onWeatherReceived(weatherData));
                    
                } else {
                    ((android.app.Activity) context).runOnUiThread(() -> 
                        callback.onError("Failed to fetch weather data. Response code: " + responseCode));
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error fetching weather data", e);
                ((android.app.Activity) context).runOnUiThread(() -> 
                    callback.onError("Error: " + e.getMessage()));
            }
        }).start();
    }
    
    private static WeatherData parseWeatherData(JSONObject jsonResponse) {
        try {
            JSONObject main = jsonResponse.getJSONObject("main");
            JSONObject weather = jsonResponse.getJSONArray("weather").getJSONObject(0);
            
            double temperature = main.getDouble("temp");
            String condition = weather.getString("main");
            String description = weather.getString("description");
            
            String simplifiedCondition;
            int weatherIcon;
            int weatherColor;
            
            if (condition.toLowerCase().equals("rain") || 
                condition.toLowerCase().equals("drizzle") || 
                condition.toLowerCase().equals("thunderstorm") ||
                condition.toLowerCase().equals("clouds")) {
                simplifiedCondition = "Rainy";
                weatherIcon = com.aurafit.AuraFitApp.R.drawable.ic_rainy;
                weatherColor = 0xFF4A90E2;
            } else {
                simplifiedCondition = "Sunny";
                weatherIcon = com.aurafit.AuraFitApp.R.drawable.ic_sunny;
                weatherColor = 0xFFFFD700;
            }
            
            return new WeatherData(simplifiedCondition, description, temperature, weatherIcon, weatherColor);
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing weather data", e);
            return new WeatherData("Forecasting", "Weather forecast", 25.0, 
                                 com.aurafit.AuraFitApp.R.drawable.ic_sunny, 0xFFFFD700);
        }
    }
    
    private static WeatherData parseWeatherDataWithGender(JSONObject jsonResponse, String userGender) {
        try {
            JSONObject main = jsonResponse.getJSONObject("main");
            JSONObject weather = jsonResponse.getJSONArray("weather").getJSONObject(0);
            
            double temperature = main.getDouble("temp");
            String condition = weather.getString("main");
            String description = weather.getString("description");
            
            String simplifiedCondition;
            int weatherIcon;
            int weatherColor;
            
            if (condition.toLowerCase().equals("rain") || 
                condition.toLowerCase().equals("drizzle") || 
                condition.toLowerCase().equals("thunderstorm") ||
                condition.toLowerCase().equals("clouds")) {
                simplifiedCondition = "Rainy";
                weatherIcon = com.aurafit.AuraFitApp.R.drawable.ic_rainy;
                weatherColor = 0xFF4A90E2;
            } else {
                simplifiedCondition = "Sunny";
                weatherIcon = com.aurafit.AuraFitApp.R.drawable.ic_sunny;
                weatherColor = 0xFFFFD700;
            }
            
            return new WeatherData(simplifiedCondition, description, temperature, weatherIcon, weatherColor, userGender);
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing weather data", e);
            return new WeatherData("Forecasting", "Weather forecast", 25.0, 
                                 com.aurafit.AuraFitApp.R.drawable.ic_sunny, 0xFFFFD700, userGender);
        }
    }
    
    public static WeatherData getDefaultWeather() {
        return new WeatherData("Forecasting", "Weather forecast", 25.0, 
                             com.aurafit.AuraFitApp.R.drawable.ic_sunny, 0xFFFFD700);
    }
}
