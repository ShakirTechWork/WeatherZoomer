package com.example.weatherwish.firebase

import java.lang.Exception

sealed class FirebaseResponse<out T> {
    data class Success<out R>(val data: R?) : FirebaseResponse<R>()
    data class Failure(val code: String, val message: String, val exception: Exception?= null) : FirebaseResponse<Nothing>()
    object Loading : FirebaseResponse<Nothing>()
}