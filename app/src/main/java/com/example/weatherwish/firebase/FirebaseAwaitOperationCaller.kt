package com.example.weatherwish.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

suspend fun <T> firebaseAwaitOperationCaller(firebaseCall: suspend () -> Task<T>): FirebaseResponse<T> {
    return try {
        val result = withContext(Dispatchers.IO) {
            firebaseCall().await()
        }

        FirebaseResponse.Success(result)
    } catch (e: FirebaseAuthException) {
        FirebaseResponse.Failure("Firebase authentication error", e.message ?: "Unknown authentication error", e)
    } catch (e: Exception) {
        FirebaseResponse.Failure("Unknown Error", e.message ?: "Unknown error", e)
    }
}
