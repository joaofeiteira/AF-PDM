package com.example.af;

import com.google.firebase.firestore.PropertyName;

public class WeatherRecord {
    public String id;
    public double latitude;
    public double longitude;
    public double temperature;
    public String condition;
    public double windSpeed;
    public String observation;
    
    @PropertyName("favorite")
    public boolean favorite;
    
    public long timestamp;

    public WeatherRecord() {
        // Construtor padrão necessário para o Firestore
    }

    public WeatherRecord(String id, double latitude, double longitude, double temperature, 
                         String condition, double windSpeed, String observation, 
                         boolean favorite, long timestamp) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.temperature = temperature;
        this.condition = condition;
        this.windSpeed = windSpeed;
        this.observation = observation;
        this.favorite = favorite;
        this.timestamp = timestamp;
    }

    @PropertyName("favorite")
    public boolean isFavorite() {
        return favorite;
    }

    @PropertyName("favorite")
    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
