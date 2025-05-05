package com.example.cropchecker.repository

import com.example.cropchecker.api.RetrofitClient
import com.example.cropchecker.models.HourlyForecast
import com.example.cropchecker.models.WeatherResponse
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherRepository {

    suspend fun getHourlyWeather(lat: Double, lon: Double, apiKey: String): Response<WeatherResponse> {
        val response = RetrofitClient.weatherApi.getHourlyForecast(lat, lon, apiKey)
        if (!response.isSuccessful) {
            println("Error: ${response.code()} - ${response.errorBody()?.string()}")
        }
        return response
    }
    fun filterForecastsForNext3Days(list: List<HourlyForecast>): Map<String, List<HourlyForecast>> {
        return list.groupBy {
            val date = Date(it.dt * 1000L)
            SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        }.entries.take(3).associate { it.toPair() }
    }


}
