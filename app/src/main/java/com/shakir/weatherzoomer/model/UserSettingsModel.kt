package com.shakir.weatherzoomer.model
// temperature in c for celsius or f for fahrenheit
//wind_speed in mph for miles per hour or kph for kilometer per hour
//precipitation in mm for millimeters or in for inches
//visibility in km for kilometer or miles for miles
data class UserSettingsModel(
    val preferred_unit: String,
    val weather_updates: WeatherUpdatesModel
) {
    // Default no-argument constructor required by Firebase
    constructor() : this("", WeatherUpdatesModel("", 0, "","",arrayListOf()))
}

