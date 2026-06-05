package com.example.af;

import android.content.Context;

public class WeatherUtils {
    public static String getConditionText(Context context, int code) {
        switch (code) {
            case 0: return context.getString(R.string.cond_clear);
            case 1: case 2: case 3: return context.getString(R.string.cond_cloudy);
            case 45: case 48: return context.getString(R.string.cond_fog);
            case 51: case 53: case 55: return context.getString(R.string.cond_drizzle);
            case 61: case 63: case 65: return context.getString(R.string.cond_rain);
            case 71: case 73: case 75: return context.getString(R.string.cond_snow);
            case 80: case 81: case 82: return context.getString(R.string.cond_showers);
            case 95: case 96: case 99: return context.getString(R.string.cond_storm);
            default: return context.getString(R.string.cond_unknown, code);
        }
    }
}
