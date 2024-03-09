package com.example.weatherapp.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import java.io.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.R
import com.example.weatherapp.activity.bottomsheet.MyBottomSheetFragment
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.model.viewmodel.WeatherViewModel
import com.example.weatherapp.model.weather_data.CurruntWeather
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomSheetFragment : MyBottomSheetFragment
    private lateinit var viewModel: WeatherViewModel
    private val LOCATION_PERMISSION_REQUEST_CODE = 101

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val syncIcon = binding.syncIcon
        syncIcon.setOnTouchListener(SwipeTouchListener())
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        requestLocationPermission()




        val locationEditText = binding.autoCompleteTextView
        locationEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val city =locationEditText.text
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(locationEditText.windowToken, 0)
                fetchWeatherByCity(city.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }


    fun showButton() {
        val syncIcon = binding.syncIcon
        syncIcon.visibility = View.VISIBLE
        syncIcon.animate().translationY(0f).alpha(1f).start()
    }


    private inner class SwipeTouchListener : View.OnTouchListener {
        private val SWIPE_THRESHOLD = 100
        private var downY: Float = 0f

        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    downY = event.y
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    val upY = event.y
                    val deltaY = upY - downY

                    if (Math.abs(deltaY) > SWIPE_THRESHOLD) {
                        if (deltaY < 0) {
                            // Swipe up detected, show the bottom sheet or perform any other action
                            animateButtonUp()
                            bottomSheetFragment = MyBottomSheetFragment()
                            val existingFragment = supportFragmentManager.findFragmentByTag(
                                MyBottomSheetFragment::class.java.simpleName) as MyBottomSheetFragment?
                            if (existingFragment != null) {
                                // If it is already added, dismiss it
                                existingFragment.dismiss()
                            }
                            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
                        }
                    }

                    return true
                }
            }
            return false
        }

        private fun animateButtonUp() {
            val syncIcon = binding.syncIcon
            syncIcon.animate()
                .translationY(-syncIcon.height.toFloat()) // Adjust the value as needed
                .alpha(0f)
                .withEndAction {
                    syncIcon.visibility = View.INVISIBLE
                }
                .start()
        }
    }
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun fetchWeatherByCity(city:String) {
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(WeatherViewModel::class.java)
        lifecycleScope.launch {
            val response = viewModel.fetchWeatherByCIty(city)
            if (response.isSuccessful) {
                val currWeather: CurruntWeather? =  response.body()
                binding.city.text = currWeather!!.name
                val temp : Int = (currWeather.main.temp -273).toInt()
                binding.temp.text = "${temp}°"
                binding.desc.text = currWeather.weather[0].description
                binding.windSpeed.text = "Wind ${currWeather.wind.speed} KM/H"
                binding.humidity.text = "Humidity ${currWeather.main.humidity}%"
                val code: Int = currWeather.cod
                val icon: String = currWeather.weather[0].icon
                when {
                    code in 201..300 -> binding.icon.setImageResource(R.drawable.thunder)
                    code in 300..399 -> binding.icon.setImageResource(R.drawable.shower)
                    code in 500..599 -> binding.icon.setImageResource(R.drawable.rain)
                    code in 600..699 -> binding.icon.setImageResource(R.drawable.snow)
                    code in 701..799 -> binding.icon.setImageResource(R.drawable.fog)
                    code == 800 && icon == "01n" -> binding.icon.setImageResource(R.drawable.clear0)
                    code in 801..804 -> binding.icon.setImageResource(R.drawable.cloudy)
                    else -> binding.icon.setImageResource(R.drawable.clear)
                }
                binding.forPermissions.visibility = View.GONE
            } else {
                val errorMessage = response.errorBody()?.string()
                Toast.makeText(this@MainActivity,"Response Body error ${errorMessage}",Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fetchCurrentLocation()
        }
    }


    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun fetchCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(WeatherViewModel::class.java)
                    lifecycleScope.launch {
                        val response = viewModel.fetchWeather(latitude, longitude)

                        if (response.isSuccessful) {
                            val currWeather: CurruntWeather? =  response.body()
                            binding.city.text = currWeather!!.name
                            val temp : Int = (currWeather.main.temp -273).toInt()
                            binding.temp.text = "${temp}°"
                            binding.desc.text = currWeather.weather[0].description
                            binding.windSpeed.text = "Wind ${currWeather.wind.speed} KM/H"
                            binding.humidity.text = "Humidity ${currWeather.main.humidity}%"
                            val code: Int = currWeather.cod
                            val icon: String = currWeather.weather[0].icon
                            when {
                                code in 201..300 -> binding.icon.setImageResource(R.drawable.thunder)
                                code in 300..399 -> binding.icon.setImageResource(R.drawable.shower)
                                code in 500..599 -> binding.icon.setImageResource(R.drawable.rain)
                                code in 600..699 -> binding.icon.setImageResource(R.drawable.snow)
                                code in 701..799 -> binding.icon.setImageResource(R.drawable.fog)
                                code == 800 && icon == "01n" -> binding.icon.setImageResource(R.drawable.clear0)
                                code in 801..804 -> binding.icon.setImageResource(R.drawable.cloudy)
                                else -> binding.icon.setImageResource(R.drawable.clear)
                            }
                            binding.forPermissions.visibility = View.GONE
                        } else {
                            val errorMessage = response.errorBody()?.string()
                            Toast.makeText(this@MainActivity,"Response Body error ${errorMessage}",Toast.LENGTH_LONG).show()
                        }
                    }

                } else {
                    Toast.makeText(this@MainActivity,"Something went wrong",Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
            }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchCurrentLocation()
                } else {
                    Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
                    openAppSettings()
                }
            }
        }
    }
    private fun openAppSettings() {
        val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        settingsIntent.data = uri
        startActivity(settingsIntent)
    }

    override fun onRestart() {
        super.onRestart()
        requestLocationPermission()
    }
}