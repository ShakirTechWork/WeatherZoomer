package com.example.weatherwish.dataParsers

import com.example.weatherwish.model.Current
import com.example.weatherwish.model.Location
import com.example.weatherwish.model.WeatherForecastModel
import com.example.weatherwish.utils.Utils

class WeatherDataParser(private val weatherForecastModel: WeatherForecastModel) {

    private var location: Location = weatherForecastModel.location
    private var current: Current = weatherForecastModel.current

    fun getSelectedDate(): String {
        return Utils.convertUnixTimeToFormattedDayAndDate(current.last_updated_epoch.toLong())
    }

    fun getLocationInfo(): Location {
        return weatherForecastModel.location
    }

    fun getCurrentInfo(): Current {
        return weatherForecastModel.current
    }

}