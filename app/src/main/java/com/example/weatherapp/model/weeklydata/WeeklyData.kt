package com.example.weatherapp.model.weeklydata

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

data class WeeklyData(
    val city: City,
    val cnt: Int,
    val cod: String,
    var list: List<WeatherForecastItem>,
    val message: Int
) {
    fun getFilteredWeatherList(): List<WeatherForecastItem> {
        return if (list.size >= 9) {
            list.subList(0, 9)
        } else {
            list
        }
    }
    fun getFilteredDailyWeatherList(): List<WeatherForecastItem> {
        val dailyWeatherList = mutableListOf<WeatherForecastItem>()
        if (list.isNotEmpty()) {
            val groupedByDay = list.groupBy { getDayFromDate(it.dt_txt) }

            groupedByDay.values.forEach { dayEntries ->
                dailyWeatherList.addAll(dayEntries) // Add all items for each day
            }
        }
        return dailyWeatherList
    }

    private fun getDayFromDate(dateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        try {
            val date = inputFormat.parse(dateTime)
            return outputFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return "Invalid Date Format"
        }
    }
}
