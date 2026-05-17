package com.example.cropchecker.models

data class WeatherResponse(
    val cod: String, // HTTP status code (e.g., "200")
    val message: Int, // Message code (usually 0 for success)
    val cnt: Int, // Number of forecasts in the list
    val list: List<HourlyForecast>
)

data class HourlyForecast(
    val dt: Long, // Unix timestamp
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Int,
    val pop: Double, // Probability of precipitation
    val sys: Sys,
    val dt_txt: String // Date and time in text format
)

data class Main(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val sea_level: Int?,
    val grnd_level: Int?,
    val humidity: Int,
    val temp_kf: Double?
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Clouds(
    val all: Int
)

data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double?
)

data class Sys(
    val pod: String // Part of the day ("d" for day, "n" for night)
)