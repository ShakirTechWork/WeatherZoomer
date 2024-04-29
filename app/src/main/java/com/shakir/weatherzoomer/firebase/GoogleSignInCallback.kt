package com.shakir.weatherzoomer.firebase

interface GoogleSignInCallback {
    fun onSuccess()
    fun onFailure(exception: Exception)
    fun onLoading()
}
