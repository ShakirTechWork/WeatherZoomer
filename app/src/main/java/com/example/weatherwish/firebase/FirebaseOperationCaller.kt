package com.example.weatherwish.firebase

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

private const val TAG = "FirebaseOperationCaller"
fun firebaseOperationCaller(firebaseCall: suspend () -> Unit): Flow<FirebaseResponse<Boolean>> = flow {
    emit(FirebaseResponse.Loading)

    try {
        withContext(Dispatchers.IO) {
            firebaseCall()
            // If the above call doesn't throw an exception, consider it a success
            emit(FirebaseResponse.Success(true))
        }
    } catch (e: Exception) {
        // Handle other exceptions
//        emit(FirebaseResponse.Failure("Unknown Error", e.message ?: "Unknown error", e))
        Log.d(TAG, "firebaseOperationCaller: ${e}")
    }
}
