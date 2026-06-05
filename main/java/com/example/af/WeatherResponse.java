package com.example.af;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("current_weather")
    public CurrentWeather currentWeather;

    public static class CurrentWeather {
        public double temperature;
        public double windspeed;
        @SerializedName("weathercode")
        public int weatherCode;
    }
}
