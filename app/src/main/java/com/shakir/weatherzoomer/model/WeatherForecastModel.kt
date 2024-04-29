package com.shakir.weatherzoomer.model

data class WeatherForecastModel(
    val alerts: Alerts,
    val current: Current,
    val forecast: Forecast,
    val location: Location
)