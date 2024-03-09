package com.example.weatherapp.model.weeklydata

data class Weather(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)