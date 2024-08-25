package com.shakir.weatherzoomer.model

data class LocationModel(
    var primaryLocation: Boolean = false,
    var location: String = ""
) {
    constructor() : this(false, "")
}

