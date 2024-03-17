package com.example.weatherwish.api

sealed class ApiResponse<out T> {
    data class Success<out R>(val data: R?) : ApiResponse<R>()
    data class Failure(val exception: Exception?= null) : ApiResponse<Nothing>()
    object Loading : ApiResponse<Nothing>()
}
