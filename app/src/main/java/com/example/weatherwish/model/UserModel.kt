package com.example.weatherwish.model

data class UserModel(
    val user_name: String,
    val user_email: String,
    val user_primary_location: String = "",
    val user_settings: UserSettingsModel = UserSettingsModel(
        "metric",
        WeatherUpdatesModel("", 0, "","", arrayListOf())
    )
) {
    // Default no-argument constructor required by Firebase
    constructor() : this("", "", "")
}