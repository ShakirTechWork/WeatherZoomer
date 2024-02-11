package com.example.weatherwish.dataParsers

import com.example.weatherwish.model.Current
import com.example.weatherwish.model.Location
import com.example.weatherwish.model.WeatherForecastModel

class WeatherDataParser(private val weatherForecastModel: WeatherForecastModel) {

    var location: Location? = null

    init {
        this.location = weatherForecastModel.location
    }

    fun getLocationInfo(): Location {
        return weatherForecastModel.location
    }

    fun getCurrentInfo(): Current {
        return weatherForecastModel.current
    }

}