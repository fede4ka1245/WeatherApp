package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity {
    private static final String TAG = "WeatherActivity";
    
    private Toolbar toolbar;
    private TextView tvRegionName;
    private TextView tvCurrentDate;
    private TextView tvTemperature;
    private TextView tvHumidity;
    private TextView tvWindSpeed;

    
    private String regionName;
    private Date currentDate;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        
        initViews();
        
        boolean coordinateMode = getIntent().getBooleanExtra("coordinateMode", false);
        
        if (coordinateMode) {
            double latitude = getIntent().getDoubleExtra("latitude", 55.7558);
            double longitude = getIntent().getDoubleExtra("longitude", 37.6173);
            regionName = String.format("%.4f, %.4f", latitude, longitude);

            if (getIntent().hasExtra("temperature")) {
                loadRealWeatherData();
            }
        } else {
            regionName = getIntent().getStringExtra("regionName");
        }
        
        currentDate = new Date();
        
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        tvRegionName.setText(regionName);
        tvCurrentDate.setText(dateFormat.format(currentDate));
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvRegionName = findViewById(R.id.tvRegionName);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWindSpeed = findViewById(R.id.tvWindSpeed);
    }
    
    private void loadRealWeatherData() {
        double temperature = getIntent().getDoubleExtra("temperature", 0.0);
        double humidity = getIntent().getDoubleExtra("humidity", 0.0);
        double windSpeed = getIntent().getDoubleExtra("windSpeed", 0.0);
        String description = getIntent().getStringExtra("description");
        
        if (tvTemperature != null) {
            tvTemperature.setText(getString(R.string.temperature, String.format("%.1f", temperature)));
        }
        if (tvHumidity != null) {
            tvHumidity.setText(getString(R.string.humidity, String.format("%.0f", humidity)));
        }
        if (tvWindSpeed != null) {
            tvWindSpeed.setText(getString(R.string.wind_speed, String.format("%.1f", windSpeed)));
        }
        
        if (description != null && !description.isEmpty() && tvRegionName != null) {
            tvRegionName.setText(regionName + " • " + description);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}