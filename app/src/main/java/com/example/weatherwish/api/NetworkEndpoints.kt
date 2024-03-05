package com.example.weatherwish.api

import com.example.weatherwish.model.WeatherData
import com.example.weatherwish.model.WeatherForecastModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NetworkEndpoints {

    //    http://api.weatherapi.com/v1/current.json?key=api_key&q=Worli&aqi=yes
    @GET("v1/current.json")
    suspend fun getCurrentWeather(
        @Query("key") key: String,
        @Query("q") location: String,
        @Query("aqi") aqi: String
    ): Response<WeatherData>


    //    http://api.weatherapi.com/v1/forecast.json?key=api_key&q=Worli&days=3&aqi=yes&alerts=yes
    @GET("v1/forecast.json")
    suspend fun forecastWeather(
        @Query("key") key: String,
        @Query("q") location: String,
        @Query("days") days: Int,
        @Query("aqi") aqi: String,
        @Query("alerts") alerts: String
    ): Response<WeatherForecastModel>

}