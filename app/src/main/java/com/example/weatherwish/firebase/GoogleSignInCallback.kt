package com.example.weatherwish.firebase

interface GoogleSignInCallback {
    fun onSuccess()
    fun onFailure(exception: Exception)
}
