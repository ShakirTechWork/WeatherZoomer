package com.example.weatherwish.dataParsers

import android.view.View
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
            "${weatherForecastData.current.temp_c.toInt()}${getUnit(ScaleOfMeasurement.TEMPERATURE)}"
        } else {
            "${weatherForecastData.forecast.forecastday[index].day.avgtemp_c.toInt()}${getUnit(ScaleOfMeasurement.TEMPERATURE)}"
        }
    }

    fun getFeelsLikeTemperature(): String {
        return if (index == 0) {
            "Feels like ${weatherForecastData.current.feelslike_c.toInt()}${getUnit(ScaleOfMeasurement.TEMPERATURE)}"
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
        if (weatherForecastData.location.name.isNotBlank()) {
            location.append("${weatherForecastData.location.name}, ")
        }
        if (weatherForecastData.location.region.isNotBlank()) {
            location.append("${weatherForecastData.location.region}, ")
        }
        if (weatherForecastData.location.country.isNotBlank()) {
            location.append(weatherForecastData.location.country)
        }
        return location.toString()
    }

    fun getHourlyTemperatureData(): List<Hour> {
        return weatherForecastData.forecast.forecastday[index].hour
    }

    fun getAirQualityGrade(): String {
        val airQuality = StringBuilder("Air Quality: ")
        return if (index == 0) {
            if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
                airQuality.append(
                    when (weatherForecastData.current.air_quality.`gb-defra-index`) {
                        1 -> "Low(1/10)"
                        2 -> "Low(2/10)"
                        3 -> "Low(3/10)"
                        4 -> "Moderate(4/10)"
                        5 -> "Moderate(5/10)"
                        6 -> "Moderate(6/10)"
                        7 -> "High(7/10)"
                        8 -> "High(8/10)"
                        9 -> "High(9/10)"
                        10 -> "Very High(10/10)"
                        else -> {
                            ""
                        }
                    }
                ).toString()
            } else {
                airQuality.append(
                    when (weatherForecastData.current.air_quality.`us-epa-index`) {
                        1 -> "Good(1/6)"
                        2 -> "Moderate(2/6)"
                        3 -> "Unhealthy for sensitive group(3/6)"
                        4 -> "Unhealthy(4/6)"
                        5 -> "Very Unhealthy(5/6)"
                        6 -> "Hazardous(6/6)"
                        else -> {
                            ""
                        }
                    }
                ).toString()
            }
        } else {
            ""
        }
    }

    fun getHumidityPercentage(): String {
        return if (index == 0) {
            "${weatherForecastData.current.humidity}%"
        } else {
            "${weatherForecastData.forecast.forecastday[index].day.avghumidity.toInt()}%"
        }
    }

    fun getWindSpeed(): String {
        return if (index == 0) {
            "${weatherForecastData.current.wind_kph} ${getUnit(ScaleOfMeasurement.SPEED)}"
        } else {
            "${weatherForecastData.forecast.forecastday[index].day.maxwind_kph} ${getUnit(ScaleOfMeasurement.SPEED)}"
        }
    }

    fun getUVIndex(): String {
        return if (index == 0) {
            when (weatherForecastData.current.uv.toInt()) {
                1 -> "Low(1/11)"
                2 -> "Low(2/11)"
                3 -> "Moderate(3/11)"
                4 -> "Moderate(4/11)"
                5 -> "Moderate(5/11)"
                6 -> "High(6/11)"
                7 -> "High(7/11)"
                8 -> "Very High(8/11)"
                9 -> "Very High(9/11)"
                10 -> "Very High(10/11)"
                11 -> "Extreme(11/11)"
                else -> ""
            }
        } else {
            when (weatherForecastData.forecast.forecastday[index].day.uv.toInt()) {
                1 -> "Low(1/11)"
                2 -> "Low(2/11)"
                3 -> "Moderate(3/11)"
                4 -> "Moderate(4/11)"
                5 -> "Moderate(5/11)"
                6 -> "High(6/11)"
                7 -> "High(7/11)"
                8 -> "Very High(8/11)"
                9 -> "Very High(9/11)"
                10 -> "Very High(10/11)"
                11 -> "Extreme(11/11)"
                else -> ""
            }
        }
    }

    fun getWindDirection(): String {
        return if (index == 0) {
            weatherForecastData.current.wind_dir
        } else {
            "" //no data available
        }
    }

    fun getSunriseTime(): String {
        return weatherForecastData.forecast.forecastday[index].astro.sunrise
    }
    fun getSunsetTime(): String {
        return weatherForecastData.forecast.forecastday[index].astro.sunset
    }

    fun getAlerts(): Pair<String, String>? {
        val alerts = weatherForecastData.alerts.alert
        return if (alerts.isNotEmpty()) {
            val alert = alerts[0]
            Pair (alert.headline, alert.instruction)
        } else {
            null
        }
    }

    private fun getUnit(scaleOfMeasurement: ScaleOfMeasurement): String {
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

}