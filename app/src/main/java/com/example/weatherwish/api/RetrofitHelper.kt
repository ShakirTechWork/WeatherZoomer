package com.example.weatherwish.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {

    private const val BASE_URL = "http://api.weatherapi.com/"

    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val httpClient = OkHttpClient.Builder().apply {
        addInterceptor(logging)
    }.build()

    fun getRetrofitInstance() : Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }

}