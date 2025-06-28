package com.example.myapplication.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class YandexMapsHelper {
    private static final String TAG = "YandexMapsHelper";

    public static void disableConnectivitySubscription(Context context) {
        try {
            Class<?> subscriptionClass = Class.forName("com.yandex.runtime.connectivity.internal.ConnectivitySubscription");
            
            Field instanceField = null;
            try {
                instanceField = subscriptionClass.getDeclaredField("instance");
                instanceField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.d(TAG, "No instance field found in ConnectivitySubscription, trying alternative approach");
            }
            
            if (instanceField != null) {
                try {
                    Object instance = instanceField.get(null);
                    if (instance != null) {
                        try {
                            Method unregisterMethod = subscriptionClass.getDeclaredMethod("unregister");
                            unregisterMethod.setAccessible(true);
                            unregisterMethod.invoke(instance);
                            Log.d(TAG, "Successfully unregistered ConnectivitySubscription");
                        } catch (Exception e) {
                            Log.d(TAG, "Failed to call unregister method: " + e.getMessage());
                        }
                        
                        instanceField.set(null, null);
                        Log.d(TAG, "Successfully set ConnectivitySubscription instance to null");
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Failed to access instance field: " + e.getMessage());
                }
            }
            
            System.setProperty("com.yandex.runtime.connectivity.disable", "true");
            Log.d(TAG, "Set system property to disable ConnectivitySubscription");
        } catch (Exception e) {
            Log.e(TAG, "Error disabling ConnectivitySubscription: " + e.getMessage(), e);
        }
    }

    public static void fixReceiverRegistration(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return;
        }
        
        try {
            System.setProperty("android.api.level", String.valueOf(Build.VERSION.SDK_INT));
            System.setProperty("android.os.build.version.sdk", String.valueOf(Build.VERSION.SDK_INT));
            
            Log.d(TAG, "Set system properties for Android API level");
        } catch (Exception e) {
            Log.e(TAG, "Error fixing receiver registration: " + e.getMessage(), e);
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return connectivityManager.getActiveNetwork() != null;
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        
        return false;
    }
}