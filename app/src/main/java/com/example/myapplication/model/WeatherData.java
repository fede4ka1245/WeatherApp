package com.example.myapplication.model;

import java.util.Date;

public class WeatherData {
    private String regionId;
    private Date date;
    private double temperature;
    private int humidity;
    private double windSpeed;
    private String description;

    public WeatherData(String regionId, Date date, double temperature, int humidity, double windSpeed, String description) {
        this.regionId = regionId;
        this.date = date;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.description = description;
    }

    public String getRegionId() {
        return regionId;
    }

    public Date getDate() {
        return date;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public String getDescription() {
        return description;
    }
}