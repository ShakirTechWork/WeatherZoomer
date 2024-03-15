package com.example.weatherwish.firebase

import com.google.firebase.auth.AuthResult

interface GoogleSignInCallback {
    fun onSuccess()
    fun onFailure(exception: Exception)
    fun onLoading()
}
