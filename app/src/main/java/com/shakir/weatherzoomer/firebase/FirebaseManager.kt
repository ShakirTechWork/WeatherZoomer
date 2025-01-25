package com.shakir.weatherzoomer.firebase

import android.app.Activity
import com.shakir.weatherzoomer.model.AppRelatedData
import com.shakir.weatherzoomer.model.SelectedTimeModel
import com.shakir.weatherzoomer.model.UserModel
import com.shakir.weatherzoomer.utils.Utils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.shakir.weatherzoomer.model.UserLocationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.CompletableFuture

class FirebaseManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    private fun enableOfflineSupport(userId: String) {
        //This method is used to make the firebase operation faster. it fetches the user data from local database internally
        val userReference = databaseReference.child("users").child(userId)
        userReference.keepSynced(true)
        /*userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userModel = snapshot.getValue(UserModel::class.java)
                // Handle the updated user model
                Utils.printDebugLog("onDataChange: $userModel")
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
            }
        })*/
    }

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

    suspend fun addUserIntoDatabase(name: String, email: String): FirebaseResponse<Boolean> {
        return try {
            val uid = auth.currentUser?.uid.toString()
            val retrievedUser = getUserByUID(uid)
            when (retrievedUser) {
                is FirebaseResponse.Success -> {
                    // User data retrieval successful

                    val userDataExists = retrievedUser.data != null
                    if (userDataExists) {
                        // User data exists, update the existing entry
                        FirebaseResponse.Success(true)
                    } else {
                        // User data doesn't exist, add a new entry
                        databaseReference.child("users")
                            .child(uid)
                            .setValue(UserModel(user_name = name))
                            .await()
                    }
                    // Return success response
                    FirebaseResponse.Success(true)
                }
                is FirebaseResponse.Failure -> {
                    // User data retrieval failed, return failure response
                    FirebaseResponse.Failure(retrievedUser.exception)
                }
                else -> {
                    FirebaseResponse.Failure(Exception("Something went wrong"))
                }
            }
        } catch (e: Exception) {
            // Exception occurred, return failure response
            FirebaseResponse.Failure(e)
        }
    }

    suspend fun getUserData(userId: String): FirebaseResponse<UserModel?> {
        return try {
            val usersRef = databaseReference.child("users").child(userId).get().await()
            if (usersRef.exists()) {
                val userModel = usersRef.getValue(UserModel::class.java)
                enableOfflineSupport(userId)
                FirebaseResponse.Success(userModel)
            } else {
                FirebaseResponse.Success(null)
            }
        } catch (e: Exception) {
            Utils.printDebugLog("dbciuebc: $e")
            FirebaseResponse.Failure(e)
        }

    }

    suspend fun addUser(userName: String): FirebaseResponse<Boolean?> {
        return try {
            databaseReference.child("users")
                .child(userName)
                .setValue(UserModel(user_name = userName))
                .await()
            FirebaseResponse.Success(true)
        } catch (e: Exception) {
            FirebaseResponse.Failure(e)
        }
    }


    suspend fun getUserByName(userName: String): FirebaseResponse<UserModel?> {
        return try {
            val usersRef = databaseReference.child("users").child(userName).get().await()
            if (usersRef.exists()) {
                val userModel = usersRef.getValue(UserModel::class.java)
//                enableOfflineSupport(userName)
                FirebaseResponse.Success(userModel)
            } else {
                FirebaseResponse.Success(null)
            }
        } catch (e: Exception) {
            Utils.printDebugLog("getUserByName: $e")
            FirebaseResponse.Failure(e)
        }

    }

    fun saveAndUpdateLocations(userId: String, newLocation: String, isCurrentLocation: Boolean): FirebaseResponse<Boolean> {
        val future = CompletableFuture<FirebaseResponse<Boolean>>()
        try {
            val userReference = FirebaseDatabase.getInstance().reference.child("users").child(userId).child("user_settings").child("locations")

            // Fetch existing locations
            userReference.get().addOnSuccessListener { snapshot ->
                val locationsMap: HashMap<String, UserLocationModel> = if (snapshot.exists()) {
                    val existingLocations = snapshot.getValue(object : GenericTypeIndicator<HashMap<String, UserLocationModel>>() {})
                    existingLocations ?: hashMapOf()
                } else {
                    hashMapOf()
                }

                Utils.printDebugLog("locationsMap___: $locationsMap")

                if (locationsMap.isNotEmpty()) {
                    // Update existing locations values
                    locationsMap.forEach { (key, value) ->
                        locationsMap[key] = value.copy(
                            selectedLocation = false,
                        )
                    }

                    if (isCurrentLocation) {
                        var isCurrentLocationFound = false
                        for (key in locationsMap.keys) {
                            val value = locationsMap[key]
                            Utils.printDebugLog("location_value: ${value?.currentLocation}")
                            if (value?.currentLocation == true) {
                                //update the current location
                                locationsMap[key] = value.copy(
                                    selectedLocation = true,
                                    location = newLocation
                                )
                                isCurrentLocationFound = true
                                break
                            }
                        }
                        Utils.printDebugLog("isCurrentLocationFound: $isCurrentLocationFound")
                        if (!isCurrentLocationFound) {
                            val locationKey = FirebaseDatabase.getInstance().reference.push().key ?: UUID.randomUUID().toString()
                            val newLocationModel = UserLocationModel(
                                selectedLocation = true,
                                currentLocation = true,
                                location = newLocation
                            )
                            locationsMap[locationKey] = newLocationModel
                        }
                    } else {
                        // Add the current location
                        val locationKey = FirebaseDatabase.getInstance().reference.push().key ?: UUID.randomUUID().toString()
                        val newLocationModel = UserLocationModel(
                            selectedLocation = true,
                            currentLocation = false,
                            location = newLocation
                        )
                        locationsMap[locationKey] = newLocationModel
                    }
                } else {
                    // Add the new location
                    val locationKey = FirebaseDatabase.getInstance().reference.push().key ?: UUID.randomUUID().toString()
                    val newLocationModel = UserLocationModel(
                        selectedLocation = true,
                        currentLocation = isCurrentLocation,
                        location = newLocation
                    )
                    locationsMap[locationKey] = newLocationModel
                }

                userReference.setValue(locationsMap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            future.complete(FirebaseResponse.Success(true))
                        } else {
                            future.complete(FirebaseResponse.Failure(task.exception))
                        }
                    }
            }.addOnFailureListener { exception ->
                future.complete(FirebaseResponse.Failure(exception))
            }
        } catch (e: Exception) {
            future.complete(FirebaseResponse.Failure(e))
        }
        return future.join()
    }


    suspend fun deleteLocation(userId: String, locationKey: String): FirebaseResponse<Boolean> {
        val future = CompletableFuture<FirebaseResponse<Boolean>>()
        try {
            Utils.printDebugLog("firebase manager location delete for user id: $userId")
            // Reference to the specific location node in the user's locations HashMap
            val userReference = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(userId)
                .child("user_settings")
                .child("locations")
                .child(locationKey)

            // Remove the location from the HashMap
            userReference.removeValue().addOnCompleteListener { task ->
                Utils.printDebugLog("task: ${task.isSuccessful}")
                if (task.isSuccessful) {
                    future.complete(FirebaseResponse.Success(true))
                } else {
                    future.complete(FirebaseResponse.Failure(task.exception))
                }
            }.addOnFailureListener { exception ->
                Utils.printDebugLog("firebase manager delete addOnFailureListener exception: $exception")
                future.complete(FirebaseResponse.Failure(exception))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Utils.printDebugLog("firebase manager delete exception: $e")
            future.complete(FirebaseResponse.Failure(e))
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

    private suspend fun getUserByUID(uid: String): FirebaseResponse<UserModel?> {
        return try {
            val userReference = databaseReference.child("users").child(uid)
            val snapshot = userReference.get().await()

            val userData = snapshot.getValue(UserModel::class.java)
            FirebaseResponse.Success(userData)
        } catch (e: Exception) {
            FirebaseResponse.Failure(e)
        }
    }

    suspend fun signOutCurrentUser(activity: Activity) {
        Utils.printDebugLog("Signing out user")
        auth.signOut()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("519620655091-pccssobbeded1frouj0h64smta168djf.apps.googleusercontent.com")
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

    suspend fun getAppRelatedData(): FirebaseResponse<AppRelatedData> {
        return try {
            val appRef = databaseReference.child("app_related_data").get().await()
            val appData = appRef.getValue(AppRelatedData::class.java)
            return FirebaseResponse.Success(appData)
        } catch (exception: Exception) {
            FirebaseResponse.Failure(exception)
        }
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
