package com.shakir.weatherzoomer.model

data class WeatherUpdatesModel(
    val update_type: String = "",
    val hourly_interval: Int = 0,
    val dnd_start_time: String = "",
    val dnd_end_time: String = "",
    val time_list: ArrayList<SelectedTimeModel> = arrayListOf()
)
