package com.example.weatherwish.exceptionHandler

class WeatherApiException(errorCode: String, message: String) : Exception(message)