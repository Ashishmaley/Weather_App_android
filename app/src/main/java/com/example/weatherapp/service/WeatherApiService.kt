package com.example.weatherapp.service

import com.example.weatherapp.model.weather_data.CurruntWeather
import com.example.weatherapp.model.weeklydata.WeeklyData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String
    ): Response<CurruntWeather>

    @GET("forecast")
    suspend fun getWeeklyWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String
    ): Response<WeeklyData>

    @GET("weather")
    suspend fun getWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String
    ): Response<CurruntWeather>
}
