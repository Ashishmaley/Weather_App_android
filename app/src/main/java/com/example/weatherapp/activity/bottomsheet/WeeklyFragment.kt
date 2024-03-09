package com.example.weatherapp.activity.bottomsheet

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.weatherapp.R
import com.example.weatherapp.adapter.HourlyWeatherAdapter
import com.example.weatherapp.adapter.WeeklyWeatherAdapter
import com.example.weatherapp.databinding.FragmentHourlyBinding
import com.example.weatherapp.databinding.FragmentWeeklyBinding
import com.example.weatherapp.model.viewmodel.WeatherViewModel
import com.example.weatherapp.model.weeklydata.WeeklyData
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class WeeklyFragment : Fragment() {

    private lateinit var binding : FragmentWeeklyBinding
    private lateinit var viewModel: WeatherViewModel
    private val LOCATION_PERMISSION_REQUEST_CODE = 101
    private lateinit var weeklyWeatherAdapter: WeeklyWeatherAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeeklyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestLocationPermission()
    }
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            fetchCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun fetchCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Log.d("longitude latitude","${latitude} - $longitude")
                    viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(
                        WeatherViewModel::class.java)
                    lifecycleScope.launch {
                        val response = viewModel.fetchWeeklyWeather(latitude, longitude)
                        if (response.isSuccessful) {
                            val weeklyData : WeeklyData? = response.body()
                            weeklyData?.let {
                                // Get the filtered list and update the adapter
                                val filteredList = it.getFilteredDailyWeatherList()
                                weeklyData.list = filteredList
                                weeklyWeatherAdapter = WeeklyWeatherAdapter(requireContext(),weeklyData)
                                binding.recyclerView.apply {
                                    layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
                                    adapter = weeklyWeatherAdapter
                                }

                                binding.recyclerView.adapter = weeklyWeatherAdapter
                            }
                        } else {
                            val errorMessage = response.errorBody()?.string()
                            Toast.makeText(requireContext(),"Response Body error ${errorMessage}",
                                Toast.LENGTH_LONG).show()
                        }
                    }

                } else {
                    Toast.makeText(requireContext(),"check location is enabled", Toast.LENGTH_LONG).show()
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
                    Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show()
                    openAppSettings()
                }
            }
        }
    }
    private fun openAppSettings() {
        val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        settingsIntent.data = uri
        startActivity(settingsIntent)
    }
}