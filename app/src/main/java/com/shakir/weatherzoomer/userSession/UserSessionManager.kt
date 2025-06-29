package com.shakir.weatherzoomer.userSession

import com.google.firebase.database.FirebaseDatabase
import com.shakir.weatherzoomer.exceptionHandling.exceptions.NewUserIdNotGeneratedException
import com.shakir.weatherzoomer.exceptionHandling.exceptions.UnknownException
import com.shakir.weatherzoomer.model.UserModel
import com.shakir.weatherzoomer.response.AppResponse
import com.shakir.weatherzoomer.utils.Utils
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

object UserSessionManager {

    var userModel: UserModel? = null
    private suspend fun generateUniqueUserId(): String? {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("users")
        repeat(3) { // Try 3 times
            Utils.printDebugLog("registerUser :: attempt = $it")
            val uniqueId = generateRandomId()
            val snapshot = databaseReference.child(uniqueId).get().await()
            if (!snapshot.exists()) {
                return uniqueId // Found a unique ID
            }
        }
        return null // Failed after 3 attempts
    }

    private fun generateRandomId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return (1..6)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }

    suspend fun registerUser(callback: (AppResponse<String?>) -> Unit) {
        Utils.printDebugLog("registerUser :: starting user registration")
        val uniqueUserId = generateUniqueUserId()
        if (uniqueUserId != null) {
            Utils.printDebugLog("registerUser :: unique user id generated: $uniqueUserId")
            Utils.printDebugLog("registerUser :: adding user into database: $uniqueUserId")
            FirebaseDatabase.getInstance().reference
                .child("users")
                .child(uniqueUserId)
                .setValue(mapOf("userId" to uniqueUserId))
                .addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        Utils.printDebugLog("registerUser :: user added into database successfully: $uniqueUserId")
                        callback(AppResponse.Success(uniqueUserId))
                    } else {
                        Utils.printDebugLog("registerUser :: failure: ${result.exception}")
                        callback(AppResponse.Failure(result.exception?: UnknownException()))
                    }
                }
        } else {
            callback(AppResponse.Failure(NewUserIdNotGeneratedException()))
        }
    }

}