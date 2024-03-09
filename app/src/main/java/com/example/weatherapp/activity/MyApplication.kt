package com.example.weatherapp.activity

import android.app.Application
import com.example.weatherapp.service.WeatherApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApplication : Application() {

    lateinit var weatherApiService: WeatherApiService
        private set

    override fun onCreate() {
        super.onCreate()
        // Initialize Retrofit
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Create an instance of the WeatherApiService interface
        weatherApiService = retrofit.create(WeatherApiService::class.java)
    }
}
