package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.util.YandexMapsHelper;
import com.example.myapplication.util.OpenMeteoWeatherService;
import com.google.android.material.button.MaterialButton;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CameraUpdateReason;
import com.yandex.mapkit.mapview.MapView;

import java.text.DecimalFormat;

public class MapActivity extends AppCompatActivity {
    private static final String TAG = "MapActivity";
    
    private MapView mapView;
    private TextView tvCoordinates;
    private MaterialButton btnShowWeather;
    private ProgressBar progressBar;

    private Point currentMapCenter;
    private DecimalFormat coordinateFormat = new DecimalFormat("#.####");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_map);
            
            mapView = findViewById(R.id.mapview);
            tvCoordinates = findViewById(R.id.tvCoordinates);
            btnShowWeather = findViewById(R.id.btnShowWeather);
            progressBar = findViewById(R.id.progressBar);

            if (!YandexMapsHelper.isNetworkAvailable(this)) {
                showNetworkErrorDialog();
                return;
            }
            
            initializeMap();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка инициализации: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showNetworkErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Проблема с сетью")
               .setMessage("Обнаружены проблемы с подключением к интернету. Яндекс.Карты могут работать некорректно. Проверьте настройки сети или попробуйте использовать VPN.")
               .setPositiveButton("Продолжить", (dialog, which) -> {
                   initializeMap();
               })
               .setNegativeButton("Выход", (dialog, which) -> {
                   finish();
               })
               .setCancelable(false)
               .show();
    }

    private void initializeMap() {
        try {
            try {
                MapKitFactory.getInstance();
                Log.d(TAG, "MapKit is properly initialized");
            } catch (Exception e) {
                Log.e(TAG, "MapKit is not initialized: " + e.getMessage(), e);
                Toast.makeText(this, "Ошибка инициализации MapKit: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            
            mapView.getMap().setMapType(com.yandex.mapkit.map.MapType.VECTOR_MAP);
            
            Point moscowPoint = new Point(55.7558, 37.6173);
            currentMapCenter = moscowPoint;
            
            mapView.getMap().move(
                    new CameraPosition(moscowPoint, 10.0f, 0.0f, 0.0f),
                    new Animation(Animation.Type.SMOOTH, 1),
                    null);

            mapView.getMap().addCameraListener(new com.yandex.mapkit.map.CameraListener() {
                @Override
                public void onCameraPositionChanged(
                        com.yandex.mapkit.map.Map map,
                        CameraPosition cameraPosition,
                        CameraUpdateReason cameraUpdateReason,
                        boolean finished) {

                    currentMapCenter = cameraPosition.getTarget();
                    updateCoordinatesDisplay();
                }
            });

            btnShowWeather.setOnClickListener(v -> {
                if (currentMapCenter != null) {
                    showWeatherWithLoader();
                } else {
                    Toast.makeText(this, "Координаты недоступны", Toast.LENGTH_SHORT).show();
                }
            });

            updateCoordinatesDisplay();
            
            Log.d(TAG, "Simple map initialized successfully");
        } catch (Exception e) {
            // Log any errors
            Log.e(TAG, "Error initializing Yandex Maps: " + e.getMessage(), e);
            Toast.makeText(this, "Ошибка инициализации карты: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.getMap().addCameraListener(new com.yandex.mapkit.map.CameraListener() {
            @Override
            public void onCameraPositionChanged(
                    com.yandex.mapkit.map.Map map,
                    CameraPosition cameraPosition,
                    CameraUpdateReason cameraUpdateReason,
                    boolean finished) {

                currentMapCenter = cameraPosition.getTarget();
                updateCoordinatesDisplay();
            }
        });
        updateCoordinatesDisplay();
    }

    private void showWeatherWithLoader() {
        btnShowWeather.setText("");
        progressBar.setVisibility(android.view.View.VISIBLE);
        btnShowWeather.setEnabled(false);

        OpenMeteoWeatherService.getCurrentWeather(currentMapCenter.getLatitude(), currentMapCenter.getLongitude())
            .thenAccept(weatherInfo -> {
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    btnShowWeather.setText("Показать погоду");
                    btnShowWeather.setEnabled(true);

                    Intent intent = new Intent(MapActivity.this, WeatherActivity.class);
                    intent.putExtra("latitude", currentMapCenter.getLatitude());
                    intent.putExtra("longitude", currentMapCenter.getLongitude());
                    intent.putExtra("coordinateMode", true);
                    intent.putExtra("temperature", weatherInfo.getTemperature());
                    intent.putExtra("humidity", weatherInfo.getHumidity());
                    intent.putExtra("windSpeed", weatherInfo.getWindSpeed());
                    intent.putExtra("pressure", weatherInfo.getPressure());
                    intent.putExtra("description", weatherInfo.getDescription());
                    startActivity(intent);

                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    progressBar.setVisibility(android.view.View.GONE);
                    btnShowWeather.setText("Показать погоду");
                    btnShowWeather.setEnabled(true);

                    Toast.makeText(this, "Ошибка загрузки погоды: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
                return null;
            });
    }

    private void updateCoordinatesDisplay() {
        if (currentMapCenter != null && tvCoordinates != null) {
            String coordinates = "Широта: " + coordinateFormat.format(currentMapCenter.getLatitude()) + 
                               "\nДолгота: " + coordinateFormat.format(currentMapCenter.getLongitude());
            tvCoordinates.setText(coordinates);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            try {
                MapKitFactory.getInstance().onStart();
                Log.d(TAG, "MapKit session started");
            } catch (Exception e) {
                Log.e(TAG, "Error starting MapKit session: " + e.getMessage(), e);
                Toast.makeText(this, "Ошибка запуска сессии MapKit: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            
            mapView.onStart();
        } catch (Exception e) {
            Log.e(TAG, "Error in onStart: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onStop() {
        try {
            mapView.onStop();
            MapKitFactory.getInstance().onStop();
            Log.d(TAG, "MapKit session stopped");
        } catch (Exception e) {
            Log.e(TAG, "Error in onStop: " + e.getMessage(), e);
        }
        super.onStop();
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}