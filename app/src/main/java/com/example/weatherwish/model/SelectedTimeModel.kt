package com.example.weatherwish.model

//data class SelectedTimeModel(
//    var time_in_millis: Long,
//    var readable_time: String
//)

data class SelectedTimeModel(
    val time_in_millis: Long = 0,
    val readable_time: String = ""
) {
    // Default no-argument constructor required by Firebase
    constructor() : this(0, "")
}
