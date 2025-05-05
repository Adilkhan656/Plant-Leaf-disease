package com.example.cropchecker.ui
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cropchecker.models.HourlyForecast
import com.example.cropchecker.models.WeatherResponse
import com.example.cropchecker.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherLiveData: LiveData<WeatherResponse> get() = _weatherData

    fun fetchWeather(lat: Double, lon: Double, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = repository.getHourlyWeather(lat, lon, apiKey)
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    if (weatherResponse != null) {

                        val filteredForecasts = filterForecastsForNext3Days(weatherResponse.list)

                        // Update LiveData with filtered forecasts
                        _weatherData.postValue(
                            weatherResponse.copy(list = filteredForecasts)
                        )
                    }
                } else {
                    println("API Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                println("Exception: ${e.message}")
            }
        }
    }

    // Utility function to filter forecasts for the next 3 days
    private fun filterForecastsForNext3Days(forecasts: List<HourlyForecast>): List<HourlyForecast> {
        val currentTime = System.currentTimeMillis() / 1000 // Current time in seconds
        val threeDaysInSeconds = 3 * 24 * 60 * 60 // 3 days in seconds
        val endTime = currentTime + threeDaysInSeconds

        return forecasts.filter { it.dt <= endTime }
    }
}