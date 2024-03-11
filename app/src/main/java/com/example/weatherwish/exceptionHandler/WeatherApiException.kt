package com.example.weatherwish.exceptionHandler

class WeatherApiException(
    val statusCode: Int,
    val errorCode: Int,
    val errorMessage: String
) : Exception("Weather API Error: HTTP $statusCode, Code $errorCode - $errorMessage")
