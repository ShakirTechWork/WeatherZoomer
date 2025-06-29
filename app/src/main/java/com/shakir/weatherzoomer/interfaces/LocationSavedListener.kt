package com.shakir.weatherzoomer.interfaces

import java.lang.Exception

interface LocationSavedListener {
    fun onSuccess()
    fun onError(exception: Exception?)
}