package com.example.weatherwish.dataParsers

import android.content.Context
import com.example.weatherwish.R
import com.example.weatherwish.constants.AppConstants
import com.example.weatherwish.constants.ScaleOfMeasurement
import com.example.weatherwish.constants.SystemOfMeasurement
import com.example.weatherwish.model.AQIData
import com.example.weatherwish.model.Alert
import com.example.weatherwish.model.AlertAvailableData
import com.example.weatherwish.model.Current
import com.example.weatherwish.model.Hour
import com.example.weatherwish.model.Location
import com.example.weatherwish.model.MoonData
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

    fun getAirQualityData(context: Context): AQIData? {
        val airText = StringBuilder("Air Quality: ")
        return if (index == 0) {
            if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
                val aqiIndexType = "Based on UK Defra Index"
                when (weatherForecastData.current.air_quality.`gb-defra-index`) {
                    1 -> AQIData(aqiIndexType, 1, airText.append("Low").toString(), context.getString(R.string.uk_aqi_text_one))
                    2 -> AQIData(aqiIndexType, 2, airText.append("Low").toString(), context.getString(R.string.uk_aqi_text_one))
                    3 -> AQIData(aqiIndexType, 3, airText.append("Low").toString(), context.getString(R.string.uk_aqi_text_one))
                    4 -> AQIData(aqiIndexType, 4, airText.append("Moderate").toString(), context.getString(R.string.uk_aqi_text_two))
                    5 -> AQIData(aqiIndexType, 5, airText.append("Moderate").toString(), context.getString(R.string.uk_aqi_text_two))
                    6 -> AQIData(aqiIndexType, 6, airText.append("Moderate").toString(), context.getString(R.string.uk_aqi_text_two))
                    7 -> AQIData(aqiIndexType, 7, airText.append("High").toString(), context.getString(R.string.uk_aqi_text_three))
                    8 -> AQIData(aqiIndexType, 8, airText.append("High").toString(), context.getString(R.string.uk_aqi_text_three))
                    9 -> AQIData(aqiIndexType, 9, airText.append("High").toString(), context.getString(R.string.uk_aqi_text_three))
                    10 -> AQIData(aqiIndexType, 10, airText.append("Very High").toString(), context.getString(R.string.uk_aqi_text_four))
                    else -> {
                        null
                    }
                }
            } else {
                val aqiIndexType = "Based on US - EPA Standard"
                when (weatherForecastData.current.air_quality.`us-epa-index`) {
                    1 -> AQIData(aqiIndexType, 1, airText.append("Good").toString(), context.getString(R.string.usa_aqi_text_one))
                    2 -> AQIData(aqiIndexType, 2, airText.append("Moderate").toString(), context.getString(R.string.usa_aqi_text_two))
                    3 -> AQIData(aqiIndexType, 3, airText.append("Unhealthy for sensitive group").toString(), context.getString(R.string.usa_aqi_text_three))
                    4 -> AQIData(aqiIndexType, 4, airText.append("Unhealthy").toString(), context.getString(R.string.usa_aqi_text_four))
                    5 -> AQIData(aqiIndexType, 5, airText.append("Very Unhealthy").toString(), context.getString(R.string.usa_aqi_text_five))
                    6 -> AQIData(aqiIndexType, 6, airText.append("Hazardous").toString(), context.getString(R.string.usa_aqi_text_six))
                    else -> {
                        null
                    }
                }
            }
        } else {
            null
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

    fun getRainPrecipitationData(): Pair<String, String>? {
        val chanceOfRain = weatherForecastData.forecast.forecastday[index].day.daily_chance_of_rain
        return if (chanceOfRain > 0) {
            if (systemOfMeasurement == SystemOfMeasurement.METRIC) {
                Pair(
                    "Chance of Rainfall: ${weatherForecastData.forecast.forecastday[index].day.daily_chance_of_rain}%",
                    "Rain Precipitation: ${weatherForecastData.forecast.forecastday[index].day.totalprecip_mm} ${
                        getUnit(ScaleOfMeasurement.PRECIPITATION)
                    }"
                )
            } else {
                Pair(
                    "Chance of Rainfall: ${weatherForecastData.forecast.forecastday[index].day.daily_chance_of_rain}%",
                    "Rain Precipitation: ${weatherForecastData.forecast.forecastday[index].day.totalprecip_in} ${
                        getUnit(ScaleOfMeasurement.PRECIPITATION)
                    }"
                )
            }
        } else {
            null
        }
    }

    fun getSnowPrecipitaionData(): Pair<String, String>? {
        val chanceOfSnowFall =
            weatherForecastData.forecast.forecastday[index].day.daily_chance_of_snow
        return if (chanceOfSnowFall > 0) {
            Pair(
                "Chance of Snowfall: ${weatherForecastData.forecast.forecastday[index].day.daily_chance_of_snow}%",
                "Snow Precipitation: ${weatherForecastData.forecast.forecastday[index].day.totalsnow_cm} cm"
            )
        } else {
            null
        }
    }

    fun getMoonData(): MoonData {
        val moonRiseTime = weatherForecastData.forecast.forecastday[index].astro.moonrise
        val moonSetTime = weatherForecastData.forecast.forecastday[index].astro.moonset
        val moonPhaseText = weatherForecastData.forecast.forecastday[index].astro.moon_phase
        val moonPhase = weatherForecastData.forecast.forecastday[index].astro.moon_phase
        val moonIlluminationPercentage = weatherForecastData.forecast.forecastday[index].astro.moon_illumination
        val moonPhaseDrawable = when (moonPhase) {
            AppConstants.MoonPhases.NEW_MOON -> R.drawable.new_moon_image
            AppConstants.MoonPhases.WAXING_CRESCENT -> R.drawable.waxing_crescent_moon_image
            AppConstants.MoonPhases.FIRST_QUARTER ->  R.drawable.first_quarter_moon_image
            AppConstants.MoonPhases.WAXING_GIBBOUS -> R.drawable.waxing_gibbous_moon_image
            AppConstants.MoonPhases.FULL_MOON -> R.drawable.full_moon_image
            AppConstants.MoonPhases.WANING_GIBBOUS -> R.drawable.waning_gibbous_moon_image
            AppConstants.MoonPhases.LAST_QUARTER -> R.drawable.last_quarter_moon_image
            AppConstants.MoonPhases.WANING_CRESCENT -> R.drawable.waning_crescent_moon_image
            else -> null
        }
        return MoonData("Illumination Percentage: $moonIlluminationPercentage%", "Moon Phase: $moonPhaseText", moonPhaseDrawable, "Moonrise: $moonRiseTime", "Moonset: $moonSetTime")
    }

    fun getAlerts2(): Triple<String, String, String>? {
        val alerts = weatherForecastData.alerts.alert
        return if (alerts.isNotEmpty()) {
            Utils.printErrorLog("alerts: $alerts")
            val alert = alerts[0]
            Triple(alert.headline, alert.areas, alert.desc)
        } else {
            null
        }
    }

    fun getAlerts(): AlertAvailableData? {
        val alerts = weatherForecastData.alerts.alert
        return if (alerts.isNotEmpty()) {
            Utils.printErrorLog("alerts: $alerts")
            val alert = alerts[0]
            if (alert.headline.isNotBlank() || alert.areas.isNotBlank() ||
                alert.desc.isNotBlank()) {
                AlertAvailableData(
                    alert.areas,
                    alert.desc,
                    "Alert start time\n${Utils.convertTimestampToReadableTime(alert.effective)}",
                    "Alert end time\n${Utils.convertTimestampToReadableTime(alert.expires)}",
                    alert.headline,
                    alert.instruction
                )
            } else {
                null
            }
        } else {
            null
        }
    }

    fun getGeminiAiPrompt(): String {
        val prompt = StringBuilder("")
        prompt.append("Average temperature is  ${getCurrentTemperature()}. ")
        prompt.append("Average humidity is ${getHumidityPercentage()}. ")
        if (getRainPrecipitationData()?.first != null && getRainPrecipitationData()?.second != null) {
            prompt.append("${getRainPrecipitationData()?.first}. ${getRainPrecipitationData()?.second}. ")
        }
        if (getSnowPrecipitaionData()?.first != null && getSnowPrecipitaionData()?.second != null) {
            prompt.append("${getSnowPrecipitaionData()?.first}. ${getSnowPrecipitaionData()?.second}. ")
        }
        prompt.append("UV Index is ${getUVIndex()}. ")
        prompt.append("Max wind speed is ${getWindSpeed()}. ")
        prompt.append("Considering all these factors what are the things that should be taken care of like below asked questions: What should be worn, specifically cloth fabric, texture, material? What should be eaten? What should be done for skin care?. What should be done for hair care?.\nGive your response in as less words as possible.")
        return prompt.toString()
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
        }
    }

}