package com.example.weatherwish.dataParsers

import com.example.weatherwish.model.Current
import com.example.weatherwish.model.Hour
import com.example.weatherwish.model.Location
import com.example.weatherwish.model.WeatherForecastModel
import com.example.weatherwish.utils.Utils

class WeatherDataParser(private val weatherForecastData: WeatherForecastModel, private val index: Int) {

    private var location: Location = weatherForecastData.location
    private var current: Current = weatherForecastData.current

    fun getSelectedDate(): String {
        return when (index) {
            0 -> {
                "Today"
            }
            1 -> {
                "${Utils.convertUnixTimeToFormattedDayAndDate(weatherForecastData.forecast.forecastday[index].date_epoch.toLong())} (Tommorrow)"
            }
            else -> {
                Utils.convertUnixTimeToFormattedDayAndDate(weatherForecastData.forecast.forecastday[index].date_epoch.toLong())
            }
        }
    }

    fun getCurrentTemperature(): String {
        return if (index == 0) {
            weatherForecastData.current.temp_c.toInt().toString()
        } else {
            weatherForecastData.forecast.forecastday[index].day.avgtemp_c.toInt().toString()
        }
    }

    fun getFeelsLikeTemperature(): String {
        return if (index == 0) {
            "Feels like ${weatherForecastData!!.current.feelslike_c.toInt()}°C"
        } else {
            ""
        }
    }

    fun getCurrentConditionText(): String {
        return if (index == 0) {
            weatherForecastData.current.condition.text
        } else {
            weatherForecastData.forecast.forecastday[index].day.condition.text
        }
    }

    fun getConditionImageUrl(): String {
        return if (index == 0) {
            weatherForecastData.current.condition.icon
        } else {
            weatherForecastData.forecast.forecastday[index].day.condition.icon
        }
    }

    fun getSelectedLocation(): String {
        val location = StringBuilder()
        if (!weatherForecastData.location.name.isNotBlank()) {
            location.append(weatherForecastData.location.name)
        }
        if (!weatherForecastData.location.region.isNotBlank()) {
            location.append(weatherForecastData.location.region)
        }
        if (!weatherForecastData.location.country.isNotBlank()) {
            location.append(weatherForecastData.location.country)
        }
        return location.toString()
    }

    fun getHourlyTemperatureData(): List<Hour> {
        return weatherForecastData.forecast.forecastday[index].hour
    }

    fun getAirQualityGrade(): String {
        val airQuality = StringBuilder("Air Quality: ")
        return airQuality.append(when (weatherForecastData.current.air_quality.`us-epa-index`) {
            1 -> "Good"
            2 -> "Moderate"
            3 -> "Unhealthy for sensitive group"
            4 -> "Unhealthy"
            5 -> "Very Unhealthy"
            6 -> "Hazardous"
            else -> {
                ""
            }
        }).toString()
    }

    fun getLocationInfo(): Location {
        return weatherForecastData.location
    }

    fun getCurrentInfo(): Current {
        return weatherForecastData.current
    }

}