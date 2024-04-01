package com.example.weatherwish.firebase

import android.app.Activity
import com.example.weatherwish.model.SelectedTimeModel
import com.example.weatherwish.model.UserModel
import com.example.weatherwish.utils.Utils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture

class FirebaseManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    suspend fun createUserWithEmailAndPassword2(
        email: String,
        password: String
    ): FirebaseResponse<AuthResult> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            if (result != null) {
                FirebaseResponse.Success(result)
            } else {
                FirebaseResponse.Failure(
                    null
                )
            }
//            {
//                if (it.isSuccessful) {
//                    FirebaseResponse.Success(it.result.user)
//                } else {
//                    FirebaseResponse.Failure("sdf", "Somemthing went wrong", null)
//                }
//            }
        } catch (e: Exception) {
            FirebaseResponse.Failure(e)
        }
    }

    fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }

    fun signInWithGoogleAccount(authCredential: AuthCredential) :Task<AuthResult> {
        return auth.signInWithCredential(authCredential)
    }

    fun addUserIntoDatabase(name: String, email: String): FirebaseResponse<Boolean> {
        return try {
            Firebase.database.reference.child("users")
                .child(auth.currentUser?.uid.toString())
                .setValue(
                    UserModel(
                        user_name = name,
                        user_email = email
                    )
                )
            FirebaseResponse.Success(true)
        } catch (e: Exception) {
            FirebaseResponse.Failure(e)
        }
    }

    suspend fun getUserData(userId: String): FirebaseResponse<UserModel?> {
        return try {
            val usersRef = databaseReference.child("users").child(userId).get().await()
            if (usersRef.exists()) {
                val userModel = usersRef.getValue(UserModel::class.java)
                FirebaseResponse.Success(userModel)
            } else {
                FirebaseResponse.Success(null)
            }
        } catch (e: Exception) {
            FirebaseResponse.Failure(e)
        }

    }

    fun updateUserPrimaryLocation(
        userId: String,
        primaryLocation: String
    ): FirebaseResponse<Boolean> {
        val future = CompletableFuture<FirebaseResponse<Boolean>>()

        try {
            val userReference = Firebase.database.reference.child("users").child(userId)
            val updateMap = mapOf<String, Any>("user_primary_location" to primaryLocation)

            userReference.updateChildren(updateMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        future.complete(FirebaseResponse.Success(true))
                    } else {
                        future.complete(
                            FirebaseResponse.Failure(task.exception)
                        )
                    }
                }

        } catch (e: Exception) {
            future.complete(
                FirebaseResponse.Failure(e)
            )
        }

        return future.join()
    }

    fun updatePeriodicWeatherUpdatesData(
        userId: String,
        intervalInHours: Int, dndStartTime: Long, dndEndTime: Long
    ): FirebaseResponse<Boolean> {
        val future = CompletableFuture<FirebaseResponse<Boolean>>()

        try {
            val userReference =
                Firebase.database.reference.child("users").child(userId).child("user_settings")
                    .child("weather_updates")
            val updateMap = mapOf<String, Any>("hourly_interval" to intervalInHours)
//            future.complete(FirebaseResponse.Success(true))
            userReference.updateChildren(updateMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Utils.printDebugLog("hourly_interval: successful")
                        future.complete(FirebaseResponse.Success(true))
                    } else {
                        Utils.printDebugLog("hourly_interval: unsuccessful")
                        future.complete(
                            FirebaseResponse.Failure(task.exception)
                        )
                    }
                }
            Utils.printDebugLog("hourly_interval: $userReference")

        } catch (e: Exception) {
            future.complete(FirebaseResponse.Failure(e))
        }

        return future.join()
    }

    suspend fun updatePeriodicWeatherUpdatesData2(
        userId: String,
        intervalInHours: Int,
        dndStartTime: String,
        dndEndTime: String
    ): FirebaseResponse<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val userReference =
                    Firebase.database.reference.child("users").child(userId).child("user_settings")
                        .child("weather_updates")
                val updateMap = mapOf<String, Any>(
                    "hourly_interval" to intervalInHours,
                    "update_type" to "periodic",
                    "dnd_start_time" to dndStartTime,
                    "dnd_end_time" to dndEndTime
                )

                userReference.updateChildren(updateMap).await()
                FirebaseResponse.Success(true)
            }
        } catch (e: Exception) {
            FirebaseResponse.Failure(e)
        }
    }

    suspend fun updateTimelyWeatherUpdatesData(
        userId: String,
        timeList: ArrayList<SelectedTimeModel>
    ): FirebaseResponse<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val userReference =
                    Firebase.database.reference.child("users").child(userId).child("user_settings")
                        .child("weather_updates")
                val updateMap = mapOf<String, Any>(
                    "update_type" to "timely",
                    "time_list" to timeList
                )

                userReference.updateChildren(updateMap).await()
                FirebaseResponse.Success(true)
            }
        } catch (e: Exception) {
            FirebaseResponse.Failure(e)
        }
    }

    suspend fun updateUserUnitPreference(
        userId: String,
        preferredUnit: String
    ): FirebaseResponse<Boolean> {
        return try {
            val userReference = Firebase.database.reference.child("users").child(userId).child("user_settings")
            val updateMap = mapOf<String, Any>("preferred_unit" to preferredUnit)
            userReference.updateChildren(updateMap).await()
            FirebaseResponse.Success(true)
        } catch (e: Exception) {
            FirebaseResponse.Failure(e)
        }
    }

    //sign in with email and password using firebase
    fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Task<AuthResult?> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    //Get the currently authenticated user
    suspend fun getCurrentLoggedInUser(): FirebaseResponse<FirebaseUser?> {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                FirebaseResponse.Success(auth.currentUser)
            } catch (e: Exception) {
                FirebaseResponse.Failure(e)
            }
        }
    }

    suspend fun signOutCurrentUser(activity: Activity) {
        Utils.printDebugLog("Signing out user")
        auth.signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("806589059954-hl0bgnug2g90qdjqstvd0jvsapdk8f35.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(activity, gso).signOut().await()
//        val mGoogleSignInClient =
//        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
//            Utils.printDebugLog("mGoogleSignInClient: Signing out user")
//        }

//        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
//            // Optional: Update UI or show a message to the user
//            val intent = Intent(this@MainActivity, LoginActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
    }

    // Example: Write data to Firebase Realtime Database
    suspend fun writeDataToDatabase(data: String) {
        return withContext(Dispatchers.IO) {
            try {
                // For example, writing data to a "data" node in the database
                databaseReference.child("data").setValue(data).await()
            } catch (e: Exception) {
                // Handle error
                throw e
            }
        }
    }

    // Example: Read data from Firebase Realtime Database
    suspend fun readDataFromDatabase(): String {
        return withContext(Dispatchers.IO) {
            return@withContext try {
                // For example, reading data from a "data" node in the database
//                databaseReference.child("data").getValue(String::class.java).await() ?: ""
                ""
            } catch (e: Exception) {
                // Handle error
                throw e
            }
        }
    }

    // Extension function to convert Task to suspend function
//    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
//        addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                continuation.resume(task.result!!)
//            } else {
//                continuation.resumeWithException(task.exception ?: Exception("Unknown exception"))
//            }
//        }
//    }
}
