package com.shakir.weatherzoomer.dataParsers

import com.shakir.weatherzoomer.constants.ScaleOfMeasurement
import com.shakir.weatherzoomer.constants.SystemOfMeasurement
import com.shakir.weatherzoomer.model.Hour
import com.shakir.weatherzoomer.utils.Utils

class WeatherHourDataParser(private val hour: Hour, private val systemOfMeasurement: SystemOfMeasurement) {

    fun getSelectedDateTime() : String {
        return if (hour.isCurrentHour) {
            "Now\n${Utils.convertUnixTimeToFormattedDayAndDate(hour.time_epoch.toLong())}"
        } else {
            Utils.convertUnixTimeToFormattedDayAndDate(hour.time_epoch.toLong())
        }
    }//done

    fun getTemperature(): String {
        return  if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
            "${hour.temp_c.toInt()}${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.TEMPERATURE)}"
        } else {
            "${hour.temp_f.toInt()}${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.TEMPERATURE)}"
        }
    }

    fun getFeelsLikeTemperature(): String {
        return  if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
            "Feels like ${hour.feelslike_c.toInt()}${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.TEMPERATURE)}"
        } else {
            "Feels like ${hour.feelslike_f.toInt()}${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.TEMPERATURE)}"
        }
    }

    fun getConditionText(): String {
        return hour.condition.text
    }//done

    fun getConditionImage(): String {
        return "https:${hour.condition.icon}"
    }

    fun getUVIndex(): String {
        return when (hour.uv.toInt()) {
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
            else -> "NA"
        }
    }//done

    fun getHumidityPercentage(): String {
        return "${hour.humidity}%"
    }//done

    fun getWindDirection(): String {
        return hour.wind_dir
    }//done

    fun getWindDegree(): String {
        return "${hour.wind_degree}°"
    }//done

    fun getWindSpeed(): String {
        return if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
            "${hour.wind_mph} ${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.SPEED)}"
        } else {
            "${hour.wind_kph} ${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.SPEED)}"
        }
    }//done

    fun getWindChill(): String {
        return if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
            "${hour.windchill_c} ${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.TEMPERATURE)}"
        } else {
            "${hour.windchill_f} ${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.TEMPERATURE)}"
        }
    }//done

    fun getRainPrecipitationData(): Pair<String, String>? {
        val chanceOfRain = hour.chance_of_rain
        return if (chanceOfRain > 0) {
            if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
                Pair(
                    "Chance of Rainfall: ${hour.chance_of_rain}%",
                    "Rain Precipitation: ${hour.precip_mm} ${
                        Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.PRECIPITATION)
                    }"
                )
            } else {
                Pair(
                    "Chance of Rainfall: ${hour.chance_of_rain}%",
                    "Rain Precipitation: ${hour.precip_in} ${
                        Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.PRECIPITATION)
                    }"
                )
            }
        } else {
            null
        }
    }

    fun getSnowPrecipitaionData(): String? {
        val chanceOfSnowFall =
            hour.chance_of_snow
        return if (chanceOfSnowFall > 0 ) {
            "Chance of Snowfall: ${hour.chance_of_snow}%"
        } else {
            null
        }
    }

    fun getDewPointTemperature(): String {
        return if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
            "${hour.dewpoint_c}${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.TEMPERATURE)}"
        } else {
            "${hour.dewpoint_f}${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.TEMPERATURE)}"
        }
    }//done

    fun getGustSpeed(): String {
        return if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
            "${hour.gust_kph}${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.SPEED)}"
        } else {
            "${hour.gust_mph}${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.SPEED)}"
        }
    }//done

    fun getHeatIndexTemperature(): String {
        return if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
            "${hour.heatindex_c}${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.TEMPERATURE)}"
        } else {
            "${hour.heatindex_f}${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.TEMPERATURE)}"
        }
    }//done

    fun getSkyCondition(): String {
        val cloud = hour.cloud.toFloat()
        return if (cloud <= 12.5) {
            "Clear (0 oktas)"
        } else if (cloud > 12.5 && cloud <= 25.0) {
            "Few (1-2 oktas) - Few clouds"
        } else if (cloud > 25.0 && cloud <= 50.0) {
            "Scattered (3-4 oktas) - Partly cloudy"
        } else if (cloud > 50.0 && cloud <= 87.5) {
            "Broken (5-7 oktas) - Mostly cloudy"
        } else if (cloud > 87.5 && cloud <= 100.0) {
            "Overcast (8 oktas) - Completely cloudy"
        } else {
            ""
        }
    }//done

    fun getPressure(): String {
        return if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
            "${hour.pressure_mb}${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.ATMOSPHERIC_PRESSURE)}"
        } else {
            "${hour.pressure_in}${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.ATMOSPHERIC_PRESSURE)}"
        }
    }//done

    fun getVisibility(): String {
        return if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
            "${hour.vis_km}${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.DISTANCE)}"
        } else {
            "${hour.vis_miles}${Utils.getUnit(systemOfMeasurement, ScaleOfMeasurement.DISTANCE)}"
        }
    }//done

}