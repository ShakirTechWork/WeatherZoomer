package com.example.weatherwish.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

//fun <T> firebaseOperationCaller(call: suspend () -> T): Flow<FirebaseResponse<T>> = flow {
//    emit(FirebaseResponse.Loading)
//
//    try {
//        val result = withContext(Dispatchers.IO) {
//            call()
//        }
//        emit(FirebaseResponse.Success(result))
//
////        val c = withContext(Dispatchers.IO) {
////            call()
////        }
////        c.let {
////            emit(FirebaseResponse.Success(c))
////        }
//    } catch (e: FirebaseAuthException) {
//        // Handle FirebaseAuthException
//        emit(FirebaseResponse.Failure("Firebase authentication error", e.message.toString(), e))
//    } catch (e: Exception) {
//        // Handle other exceptions
//        emit(FirebaseResponse.Failure("Unknown Error",e.message ?: "Unknown error", e))
//    }
//}

//suspend fun <T> firebaseOperationCaller(firebaseCall: suspend () -> Task<T>): Flow<FirebaseResponse<T>> = flow {
//    emit(FirebaseResponse.Loading)
//
//    try {
//        val result = withContext(Dispatchers.IO) {
//            firebaseCall().await()
//        }
//        emit(FirebaseResponse.Success(result))
//
//    } catch (e: FirebaseAuthException) {
//        // Handle FirebaseAuthException
//        emit(FirebaseResponse.Failure("Firebase authentication error", e.message ?: "Unknown authentication error", e))
//    } catch (e: Exception) {
//        // Handle other exceptions
//        emit(FirebaseResponse.Failure("Unknown Error", e.message ?: "Unknown error", e))
//    }
//}

suspend fun <T> firebaseOperationAwaitCaller(firebaseCall: suspend () -> Task<T>): Flow<FirebaseResponse<T>> = flow {
    emit(FirebaseResponse.Loading)

    try {
        val result = withContext(Dispatchers.IO) {
            firebaseCall().await()
        }

        // Emit success with result (even if it's Unit)
        emit(FirebaseResponse.Success(result))
    } catch (e: Exception) {
        // Handle other exceptions
        emit(FirebaseResponse.Failure(e))
    }
}

