package com.example.af;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.af.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private WeatherApi weatherApi;
    private FirebaseFirestore db;
    
    private double currentLat, currentLon, currentTemp, currentWind;
    private int currentConditionCode;
    private boolean isDataLoaded = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherApi = retrofit.create(WeatherApi.class);

        binding.btnRefresh.setOnClickListener(v -> checkLocationPermission());
        binding.btnSave.setOnClickListener(v -> saveQuery());
        binding.btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 
                LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

    private void getLastLocation() {
        binding.progressBar.setVisibility(View.VISIBLE);
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLon = location.getLongitude();
                    binding.tvLocation.setText(getString(R.string.location_format, currentLat, currentLon));
                    fetchWeatherData(currentLat, currentLon);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.error_location, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) {
            binding.progressBar.setVisibility(View.GONE);
            Toast.makeText(this, R.string.error_permission, Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchWeatherData(double lat, double lon) {
        weatherApi.getCurrentWeather(lat, lon, true, "auto").enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().currentWeather != null) {
                    WeatherResponse.CurrentWeather weather = response.body().currentWeather;
                    currentTemp = weather.temperature;
                    currentWind = weather.windspeed;
                    currentConditionCode = weather.weatherCode;
                    
                    binding.tvTemperature.setText(getString(R.string.temp_format, currentTemp));
                    binding.tvApparentTemp.setText(getString(R.string.apparent_temp_format, currentTemp));
                    binding.tvWindSpeed.setText(getString(R.string.wind_format, currentWind));
                    binding.tvCondition.setText(getString(R.string.condition_format, WeatherUtils.getConditionText(MainActivity.this, currentConditionCode)));
                    
                    isDataLoaded = true;
                } else {
                    Toast.makeText(MainActivity.this, R.string.error_weather_data, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, getString(R.string.connection_failure, t.getMessage()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveQuery() {
        if (!isDataLoaded) {
            Toast.makeText(this, R.string.load_first, Toast.LENGTH_SHORT).show();
            return;
        }

        String observation = "";
        if (binding.etObservation.getText() != null) {
            observation = binding.etObservation.getText().toString();
        }
        
        // Garante a captura do estado atual do checkbox no momento do clique
        final boolean isFavorite = binding.cbFavorite.isChecked();

        String id = db.collection("queries").document().getId();
        long timestamp = new Date().getTime();

        WeatherRecord record = new WeatherRecord(id, currentLat, currentLon, currentTemp, 
                WeatherUtils.getConditionText(this, currentConditionCode), currentWind,
                observation, isFavorite, timestamp);

        if (id != null) {
            db.collection("queries").document(id).set(record).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, R.string.save_success, Toast.LENGTH_SHORT).show();
                    binding.etObservation.setText("");
                    binding.cbFavorite.setChecked(false);
                } else {
                    Toast.makeText(MainActivity.this, R.string.save_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
