package com.example.weatherapp.model.viewmodel

import androidx.lifecycle.AndroidViewModel
import com.example.weatherapp.activity.MyApplication
import android.app.Application
import com.example.weatherapp.service.WeatherApiService
import com.example.weatherapp.model.weather_data.CurruntWeather
import com.example.weatherapp.model.weeklydata.WeeklyData
import retrofit2.Response

class WeatherViewModel (application: Application) : AndroidViewModel(application) {

    private val weatherApiService: WeatherApiService =
        (application as MyApplication).weatherApiService

    suspend fun fetchWeather(latitude: Double, longitude: Double): Response<CurruntWeather> {
        return weatherApiService.getCurrentWeather(latitude, longitude, "f0f2dff6e79acd8300796cd23936cf1c")
    }
    suspend fun fetchWeeklyWeather(latitude: Double, longitude: Double): Response<WeeklyData> {
        return weatherApiService.getWeeklyWeather(latitude, longitude, "f0f2dff6e79acd8300796cd23936cf1c")
    }
    suspend fun fetchWeatherByCIty(city:String): Response<CurruntWeather> {
        return weatherApiService.getWeatherByCity(city, "f0f2dff6e79acd8300796cd23936cf1c")
    }
}