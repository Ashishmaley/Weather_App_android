package com.example.weatherapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.databinding.WeeklyitemBinding
import com.example.weatherapp.model.weeklydata.WeatherForecastItem
import com.example.weatherapp.model.weeklydata.WeeklyData
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class WeeklyWeatherAdapter(context: Context,weeklyData: WeeklyData) : RecyclerView.Adapter<WeeklyWeatherAdapter.WeeklyViewHolder>() {
    private val WeeklyWeatherList : List<WeatherForecastItem> = weeklyData.list

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeeklyViewHolder {
        val binding = WeeklyitemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WeeklyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeeklyViewHolder, position: Int) {
        val weeklyWeatherItem = WeeklyWeatherList[position]
        holder.bind(weeklyWeatherItem)
    }

    override fun getItemCount(): Int {
        return WeeklyWeatherList.size
    }

    inner class WeeklyViewHolder(private val binding: WeeklyitemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(weatherItem: WeatherForecastItem) {

            val date = formatDateAndTime(weatherItem.dt_txt)
            val time = formatTime(weatherItem.dt_txt)
            binding.date.text = date
            binding.time.text = time
            val temp : Int = (weatherItem.main.temp-273).toInt()
            binding.temp.text = "${temp}°C"
            val code:Int = weatherItem.weather[0].id
            when {
                code in 201..300 -> binding.icon.setImageResource(R.drawable.thunder)
                code in 300..399 -> binding.icon.setImageResource(R.drawable.shower)
                code in 500..599 -> binding.icon.setImageResource(R.drawable.rain)
                code in 600..699 -> binding.icon.setImageResource(R.drawable.snow)
                code in 701..799 -> binding.icon.setImageResource(R.drawable.fog)
                code == 800 && weatherItem.weather[0].icon == "01n" -> binding.icon.setImageResource(R.drawable.clear0)
                code in 801..804 -> binding.icon.setImageResource(R.drawable.cloudy)
                else -> binding.icon.setImageResource(R.drawable.clear)
            }
            }
        }
    fun formatDateAndTime(inputDateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())

        // Set the time zone for both input and output formats
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        outputFormat.timeZone = TimeZone.getDefault()

        try {
            val date = inputFormat.parse(inputDateTime)
            return outputFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return "Invalid Date Format"
        }
    }
    fun formatTime(inputDateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        // Set the time zone for both input and output formats
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        outputFormat.timeZone = TimeZone.getDefault()

        try {
            val date = inputFormat.parse(inputDateTime)
            return outputFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return "Invalid Date Format"
        }
    }

}
