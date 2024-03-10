package com.example.weatherwish.model

data class MoonData(
    var moon_illumination_percentage: String?,
    var moon_phase_text: String?,
    var moon_phase_drawable: Int?,
    var moon_rise_time: String?,
    var moon_set_time: String?
)