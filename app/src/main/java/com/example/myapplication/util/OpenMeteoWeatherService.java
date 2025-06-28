package com.example.myapplication.util;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import okhttp3.Dns;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OpenMeteoWeatherService {
    private static final String TAG = "OpenMeteoWeatherService";
    private static final String BASE_URL_HTTPS = "https://api.open-meteo.com/v1/forecast";
    private static final String BASE_URL_HTTP = "http://api.open-meteo.com/v1/forecast";
    private static final String BASE_URL_IP = "http://188.40.99.226/v1/forecast";
    
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .dns(new Dns() {
                @Override
                public List<InetAddress> lookup(String hostname) throws UnknownHostException {
                    if ("api.open-meteo.com".equals(hostname)) {
                        // Try to resolve using Google DNS and Cloudflare DNS
                        try {
                            return Arrays.asList(InetAddress.getByName("188.40.99.226")); // Open-Meteo IP
                        } catch (UnknownHostException e) {
                            Log.w(TAG, "Failed to resolve with hardcoded IP, trying default DNS");
                        }
                    }
                    return Dns.SYSTEM.lookup(hostname);
                }
            })
            .build();

    public static class WeatherInfo {
        private double temperature;
        private double humidity;
        private double windSpeed;
        private double pressure;
        private String description;
        private int weatherCode;
        
        public WeatherInfo(double temperature, double humidity, double windSpeed, 
                          double pressure, String description, int weatherCode) {
            this.temperature = temperature;
            this.humidity = humidity;
            this.windSpeed = windSpeed;
            this.pressure = pressure;
            this.description = description;
            this.weatherCode = weatherCode;
        }
        
        public double getTemperature() { return temperature; }
        public double getHumidity() { return humidity; }
        public double getWindSpeed() { return windSpeed; }
        public double getPressure() { return pressure; }
        public String getDescription() { return description; }
        public int getWeatherCode() { return weatherCode; }
    }
    
    public static CompletableFuture<WeatherInfo> getCurrentWeather(double latitude, double longitude) {
        return CompletableFuture.supplyAsync(() -> {
            String[] urls = {BASE_URL_HTTPS, BASE_URL_HTTP, BASE_URL_IP};
            
            for (String baseUrl : urls) {
                try {
                    String urlString = String.format(java.util.Locale.US,
                        "%s?latitude=%.2f&longitude=%.2f&hourly=temperature_2m,relativehumidity_2m,windspeed_10m",
                        baseUrl, latitude, longitude
                    );
                    
                    Log.d(TAG, "Trying URL: " + urlString);
                    
                    Request request = new Request.Builder()
                            .url(urlString)
                            .addHeader("User-Agent", "WeatherApp/1.0")
                            .addHeader("Accept", "application/json")
                            .build();
                    
                    Log.d(TAG, "Request created, attempting to execute...");
                    
                    try (Response response = client.newCall(request).execute()) {
                        if (!response.isSuccessful()) {
                            Log.w(TAG, "HTTP error code: " + response.code() + " for URL: " + baseUrl);
                            continue;
                        }
                        
                        String responseBody = response.body().string();
                        Log.d(TAG, "Raw response: " + responseBody);
                
                        JsonParser parser = new JsonParser();
                        JsonObject jsonResponse = parser.parse(responseBody).getAsJsonObject();
                        JsonObject hourly = jsonResponse.getAsJsonObject("hourly");
                        
                        double temperature = hourly.getAsJsonArray("temperature_2m").get(0).getAsDouble();
                        double humidity = hourly.getAsJsonArray("relativehumidity_2m").get(0).getAsDouble();
                        double windSpeed = hourly.getAsJsonArray("windspeed_10m").get(0).getAsDouble();
                        
                        double pressure = 1013.25;
                        String description = "Текущая погода";
                        int weatherCode = 0;
                        
                        Log.d(TAG, "Weather data fetched successfully from " + baseUrl + ": T=" + temperature + "°C, H=" + humidity + "%, W=" + windSpeed + "km/h");
                        return new WeatherInfo(temperature, humidity, windSpeed, pressure, description, weatherCode);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to fetch from " + baseUrl + ": " + e.getMessage());
                }
            }
            
            Log.e(TAG, "All weather API endpoints failed");
            throw new RuntimeException("Failed to fetch weather data from all endpoints");
        });
    }
}