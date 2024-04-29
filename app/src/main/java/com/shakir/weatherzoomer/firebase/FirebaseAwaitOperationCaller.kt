package com.shakir.weatherzoomer.firebase

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

suspend fun <T> firebaseAwaitOperationCaller(firebaseCall: suspend () -> Task<T>): FirebaseResponse<T> {
    return try {
        val result = withContext(Dispatchers.IO) {
            firebaseCall().await()
        }

        FirebaseResponse.Success(result)
    } catch (e: Exception) {
        FirebaseResponse.Failure(e)
    }
}
