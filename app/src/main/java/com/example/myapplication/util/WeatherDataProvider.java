package com.example.myapplication.util;

import com.example.myapplication.model.WeatherData;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;


public class WeatherDataProvider {
    private static final String TAG = "WeatherDataProvider";
    private static final Random random = new Random();

    public static WeatherData getWeatherData(String regionId, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        
        double baseTemp;
        switch (month) {
            case Calendar.DECEMBER:
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
                baseTemp = -15; // Winter
                break;
            case Calendar.MARCH:
            case Calendar.APRIL:
            case Calendar.MAY:
                baseTemp = 5;
                break;
            case Calendar.JUNE:
            case Calendar.JULY:
            case Calendar.AUGUST:
                baseTemp = 20; // Summer
                break;
            case Calendar.SEPTEMBER:
            case Calendar.OCTOBER:
            case Calendar.NOVEMBER:
                baseTemp = 5; // Fall
                break;
            default:
                baseTemp = 0;
        }
        
        // Add some randomness to the temperature
        double temperature = baseTemp + (random.nextDouble() * 10) - 5;
        
        // Generate random humidity (40-90%)
        int humidity = 40 + random.nextInt(50);
        
        // Generate random wind speed (0-10 m/s)
        double windSpeed = random.nextDouble() * 10;
        
        // Generate a description based on temperature
        String description;
        if (temperature < -10) {
            description = "Очень холодно, снег";
        } else if (temperature < 0) {
            description = "Холодно, возможен снег";
        } else if (temperature < 10) {
            description = "Прохладно, переменная облачность";
        } else if (temperature < 20) {
            description = "Тепло, малооблачно";
        } else {
            description = "Жарко, солнечно";
        }
        
        return new WeatherData(regionId, date, temperature, humidity, windSpeed, description);
    }
}