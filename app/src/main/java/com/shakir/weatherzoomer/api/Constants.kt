package com.shakir.weatherzoomer.api

import android.util.Log
import com.shakir.weatherzoomer.exceptionHandler.WeatherApiException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import kotlin.coroutines.coroutineContext

private const val TAG = "Constants"

@OptIn(ExperimentalStdlibApi::class)
fun <T> result(call: suspend () -> Response<T>): Flow<ApiResponse<T?>> = flow {

    emit(ApiResponse.Loading)

    try {
        Log.d(TAG, "result123: called on: ${Thread.currentThread().name}")
        Log.d(TAG, "result123: called on: ${coroutineContext[CoroutineDispatcher.Key]}")

//        val c = call()
        val c = withContext(Dispatchers.IO) {
            Log.d(TAG, "result123: API called on: ${Thread.currentThread().name}")
            Log.d(TAG, "result123: API called on: ${kotlin.coroutines.coroutineContext[CoroutineDispatcher.Key]}")
            call()
        }
        c.let {
            if (c.isSuccessful) {
                emit(ApiResponse.Success(it.body()))
            } else {
                c.errorBody()?.let {error ->
                    error.close()
//                    val jsonObject = JSONObject(error)
//                    val errorCode = jsonObject.getJSONObject("error").getInt("code")
//                    val errorMessage = jsonObject.getJSONObject("error").getString("message")
//                    emit(ApiResponse.Failure(error.toString()))

                    val errorJsonString = error.string() // Assuming error.string() is used to get the JSON string
                    val jsonObject = JSONObject(errorJsonString)
                    val errorCode = jsonObject.getJSONObject("error").getInt("code")
                    val errorMessage = jsonObject.getJSONObject("error").getString("message")
                    emit(ApiResponse.Failure(WeatherApiException(201, errorCode, errorMessage)))

                }
            }
        }

    } catch (exception: Exception) {
        Log.e(TAG, "API_Error: $exception" )
        emit(ApiResponse.Failure(exception))
    }

}