package com.example.cropchecker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.cropchecker.adapters.PredictionAdapter
import com.example.cropchecker.databinding.FragmentHomeBinding
import com.example.cropchecker.models.HourlyForecast
import com.example.cropchecker.models.Prediction
import com.example.cropchecker.models.WeatherResponse
import com.example.cropchecker.ui.MainViewModel
import com.example.cropchecker.ui.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import isomora.com.greendoctor.TensorFlowClassifier
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyAdapter: PredictionAdapter
    private lateinit var viewModel: MainViewModel
    private lateinit var weatherViewModel: WeatherViewModel
    private var sliderTimer: Timer? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = MainViewModel.create(requireContext())

        historyAdapter = PredictionAdapter(
            predictions = mutableListOf(),
            onDeleteClickListener = { prediction -> viewModel.deletePrediction(prediction) },
            onItemClickListener = { prediction -> showPredictionDialog(prediction) }
        )

        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }

        viewModel.allPredictions.observe(viewLifecycleOwner) { predictions ->
            historyAdapter.submitList(predictions)
        }

        val imageList = arrayListOf(
            SlideModel(R.drawable.image1, ScaleTypes.FIT),
            SlideModel(R.drawable.image2, ScaleTypes.FIT),
            SlideModel(R.drawable.image3, ScaleTypes.FIT),
            SlideModel(R.drawable.image4, ScaleTypes.FIT),
            SlideModel(R.drawable.image5, ScaleTypes.FIT),
            SlideModel(R.drawable.image6, ScaleTypes.FIT),
            SlideModel(R.drawable.image7, ScaleTypes.FIT)
        )
        view.findViewById<ImageSlider>(R.id.image_slider).setImageList(imageList)

        weatherViewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        // Observe weather data
        weatherViewModel.weatherLiveData.observe(viewLifecycleOwner) { weatherResponse ->
            if (weatherResponse != null) {
                updateUI(weatherResponse)
            } else {
                Log.e("HomeFragment", "Failed to fetch weather data.")
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (!checkLocationPermission()) {
            requestLocationPermission()
        } else {
            fetchCurrentLocation()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation()
            } else {
                Log.e("HomeFragment", "Location permission denied")
            }
        }
    }

    private fun fetchCurrentLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
                    fetchWeatherForLocation(latitude, longitude)
                } else {
                    Log.e("Location", "Unable to retrieve location")
                }
            }.addOnFailureListener { exception ->
                Log.e("Location", "Error fetching location: ${exception.message}")
            }
        }
    }

    private fun fetchWeatherForLocation(latitude: Double, longitude: Double) {
        val apiKey = "b795cd83bd3e9fc538b2b1ee30b0a7d5"
        weatherViewModel.fetchWeather(latitude, longitude, apiKey)
    }

    private fun updateUI(weatherResponse: WeatherResponse) {
        val firstForecast = weatherResponse.list.firstOrNull()
        if (firstForecast != null) {
            binding.currentTemp.text =
                "${firstForecast.main.temp.toInt()}°C - ${firstForecast.weather.first().description}"
            setLottieAnimation(
                firstForecast.weather.first().main.lowercase(Locale.getDefault()),
                binding.weatherAnimation
            )

            val groupedForecasts = filterForecastsForNext3Days(weatherResponse.list)
            update3DayForecast(groupedForecasts)
        } else {
            Log.e("HomeFragment", "No forecasts available")
        }
    }

    private fun filterForecastsForNext3Days(list: List<HourlyForecast>): Map<String, List<HourlyForecast>> {
        return list.groupBy {
            val date = Date(it.dt * 1000L)
            SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        }.entries
            .take(3)
            .associate { it.toPair() }
    }

    private fun update3DayForecast(groupedForecasts: Map<String, List<HourlyForecast>>) {
        val forecastViews = listOf(
            Triple(R.id.dayLabel1, R.id.dayTemp1, R.id.dayCondition1),
            Triple(R.id.dayLabel2, R.id.dayTemp2, R.id.dayCondition2),
            Triple(R.id.dayLabel3, R.id.dayTemp3, R.id.dayCondition3)
        )

        groupedForecasts.entries.forEachIndexed { index, entry ->
            if (index < forecastViews.size) {
                val (labelId, tempId, iconId) = forecastViews[index]
                val day = entry.key
                val forecastForDay = entry.value
                val maxTemp = forecastForDay.maxOf { it.main.temp }.toInt()
                val condition = forecastForDay.first().weather.first().main

                binding.root.findViewById<TextView>(labelId)?.text = day
                binding.root.findViewById<TextView>(tempId)?.text = "$maxTemp°C"
                binding.root.findViewById<TextView>(iconId)?.text = getWeatherIcon(condition)
            }
        }
    }

    private fun setLottieAnimation(weatherCondition: String, lottieView: LottieAnimationView) {
        when (weatherCondition) {
            "clear", "sunny" -> lottieView.setAnimation(R.raw.sunny)
            "rain", "drizzle", "thunderstorm" -> lottieView.setAnimation(R.raw.rain)
            "clouds", "overcast" -> lottieView.setAnimation(R.raw.cloud)
            "fog", "mist" -> lottieView.setAnimation(R.raw.foggy)
            else -> lottieView.setAnimation(R.raw.sunny)
        }
        lottieView.playAnimation()
    }

    private fun getWeatherIcon(condition: String): String {
        return when (condition.lowercase(Locale.getDefault())) {
            "clear", "sunny" -> "☀️"
            "rain", "drizzle" -> "🌧️"
            "clouds", "overcast" -> "☁️"
            "fog", "mist" -> "🌫️"
            "thunderstorm" -> "⛈️"
            else -> "❓"
        }
    }

    private fun showPredictionDialog(prediction: Prediction) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_history_detail, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.imageViewHistory)
        val timeView = dialogView.findViewById<TextView>(R.id.textViewTime)
        val predictionView = dialogView.findViewById<TextView>(R.id.textViewPrediction)
        val remedyView = dialogView.findViewById<TextView>(R.id.textViewRemedy)

        Glide.with(requireContext()).load(prediction.imageUrl).into(imageView)
        timeView.text = "Time: ${prediction.timestamp}"
        predictionView.text =
            "Prediction: ${prediction.diseaseName} (${String.format("%.2f", prediction.confidence * 100)}%)"

        val remedyList = TensorFlowClassifier.remedies[prediction.diseaseName.lowercase()]
            ?: listOf("No remedy available.")
        val remedyFormatted = remedyList.joinToString("<br>• ", prefix = "<b>Remedy:</b><br>• ")
        remedyView.text = HtmlCompat.fromHtml(remedyFormatted, HtmlCompat.FROM_HTML_MODE_LEGACY)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Prediction Details")
            .setView(dialogView)
            .setPositiveButton("OK", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(resources.getColor(android.R.color.black, null))
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sliderTimer?.cancel()
        sliderTimer = null
        _binding = null
    }
}
