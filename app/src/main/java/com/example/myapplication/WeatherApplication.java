package com.example.myapplication;

import android.app.Application;
import android.util.Log;

import com.example.myapplication.util.YandexMapsHelper;
import com.yandex.mapkit.MapKitFactory;

import java.util.logging.Logger;

public class WeatherApplication extends Application {
    private static final String TAG = "WeatherApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            YandexMapsHelper.disableConnectivitySubscription(this);
            YandexMapsHelper.fixReceiverRegistration(this);
            
            String apiKey = "add api key here"; // Hardcoded api key because of problems with initializtion
            MapKitFactory.setApiKey(apiKey);
            Log.d(TAG, "Yandex Maps API key set successfully: " + apiKey);
            
            MapKitFactory.initialize(this);
            Log.d(TAG, "Yandex Maps initialized successfully");

            suppressLocationPermissionErrors();
        } catch (Exception e) {
            Log.e(TAG, "Error in application initialization: " + e.getMessage(), e);
        }
    }

    private void suppressLocationPermissionErrors() {
        try {
            Logger locationLogger = Logger.getLogger("com.yandex.runtime.sensors.internal.LastKnownLocation");
            locationLogger.setFilter(record ->
                !record.getMessage().contains("failed to get last known location") &&
                !record.getMessage().contains("ACCESS_FINE_LOCATION") &&
                !record.getMessage().contains("ACCESS_COARSE_LOCATION")
            );
            
            Logger connectivityLogger = Logger.getLogger("com.yandex.runtime.connectivity.internal.ConnectivitySubscription");
            connectivityLogger.setFilter(record ->
                !record.getMessage().contains("Cannot register receiver") &&
                !record.getMessage().contains("SecurityException") &&
                !record.getMessage().contains("RECEIVER_EXPORTED") &&
                !record.getMessage().contains("RECEIVER_NOT_EXPORTED")
            );
            
            Logger runtimeLogger = Logger.getLogger("com.yandex.runtime");
            runtimeLogger.setFilter(record ->
                !record.getMessage().contains("SecurityException") &&
                !record.getMessage().contains("permission")
            );
            
            Log.d(TAG, "Yandex Maps errors suppressed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error suppressing Yandex Maps errors: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}