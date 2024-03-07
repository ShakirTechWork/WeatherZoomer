package com.example.weatherwish.dataParsers

import com.example.weatherwish.constants.AppConstants
import com.example.weatherwish.constants.ScaleOfMeasurement
import com.example.weatherwish.constants.SystemOfMeasurement
import com.example.weatherwish.model.Current
import com.example.weatherwish.model.Hour
import com.example.weatherwish.model.Location
import com.example.weatherwish.model.WeatherForecastModel
import com.example.weatherwish.utils.Utils

class WeatherDataParser(
    private val weatherForecastData: WeatherForecastModel,
    private val index: Int,
    private val systemOfMeasurement: SystemOfMeasurement
) {

    private var location: Location = weatherForecastData.location
    private var current: Current = weatherForecastData.current

    private var systemOfUnit: SystemOfMeasurement? = null

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
        return airQuality.append(
            when (weatherForecastData.current.air_quality.`us-epa-index`) {
                1 -> "Good"
                2 -> "Moderate"
                3 -> "Unhealthy for sensitive group"
                4 -> "Unhealthy"
                5 -> "Very Unhealthy"
                6 -> "Hazardous"
                else -> {
                    ""
                }
            }
        ).toString()
    }

    fun getHumidityGrade(): String {
        return if (index == 0) {
            weatherForecastData.current.humidity.toString()
        } else {
            weatherForecastData.forecast.forecastday[index].day.avghumidity.toString()
        }
    }

    fun getUnit(scaleOfMeasurement: ScaleOfMeasurement): String {
        return when (scaleOfMeasurement) {
            ScaleOfMeasurement.TEMPERATURE -> {
                if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
                    AppConstants.Units.DEGREE_CELSIUS //celsius
                } else {
                    AppConstants.Units.DEGREE_FAHRENHEIT //fahrenheit
                }
            }

            ScaleOfMeasurement.SPEED -> {
                if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
                    AppConstants.Units.KILOMETERS_PER_HOUR //kilometers per hour
                } else {
                    AppConstants.Units.MILES_PER_HOUR //miles per hour
                }
            }

            ScaleOfMeasurement.PRECIPITATION -> {
                if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
                    AppConstants.Units.MILLIMETERS //millimetres
                } else {
                    AppConstants.Units.INCHES //inches
                }
            }

            ScaleOfMeasurement.DISTANCE -> {
                if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
                    AppConstants.Units.KILOMETERS //kilometer
                } else {
                    AppConstants.Units.MILES //miles
                }
            }

            else -> {
                ""
            }
        }
    }

    fun getLocationInfo(): Location {
        return weatherForecastData.location
    }

    fun getCurrentInfo(): Current {
        return weatherForecastData.current
    }

}