package com.shakir.weatherzoomer.response

import java.lang.Exception

sealed class AppResponse<out T> {
    data class Success<out R>(val data: R?) : AppResponse<R>()
    data class Failure(val exception: Exception) : AppResponse<Nothing>()
}