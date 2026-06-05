package com.example.af;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("v1/forecast")
    Call<WeatherResponse> getCurrentWeather(
        @Query("latitude") double latitude,
        @Query("longitude") double longitude,
        @Query("current_weather") boolean currentWeather,
        @Query("timezone") String timezone
    );
}
