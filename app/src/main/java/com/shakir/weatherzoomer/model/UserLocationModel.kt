package com.shakir.weatherzoomer.model

data class UserLocationModel(
    var selectedLocation: Boolean = false,
    var currentLocation: Boolean = false,
    var location: String = ""
)
