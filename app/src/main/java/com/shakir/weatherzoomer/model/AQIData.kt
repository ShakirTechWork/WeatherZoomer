package com.shakir.weatherzoomer.model

data class AQIData(
    var aqi_index_type: String,
    var index: Int,
    var text: String,
    var message: String,
)
