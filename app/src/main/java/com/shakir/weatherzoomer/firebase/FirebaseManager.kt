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
import com.shakir.weatherzoomer.exceptionHandling.exceptions.UnknownException
import com.shakir.weatherzoomer.interfaces.LocationSavedListener
import com.shakir.weatherzoomer.model.UserLocationModel
import com.shakir.weatherzoomer.response.AppResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val usersListDatabaseReference: DatabaseReference = databaseReference.child(FirebaseConstants.USERS)

    private fun enableOfflineSupport(userId: String) {
        //This method is used to make the firebase operation faster. it fetches the user data from local database internally
        val userReference = usersListDatabaseReference.child(userId)
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
//                        databaseReference.child("users")
//                            .child(uid)
//                            .setValue(UserModel(user_name = name))
//                            .await()
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
            val usersRef = usersListDatabaseReference.child(userId).get().await()
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

    suspend fun getLoggedInUserData(userId: String): AppResponse<UserModel?> {
        return try {
            val usersRef = usersListDatabaseReference.child(userId).get().await()
            if (usersRef.exists()) {
                val userModel = usersRef.getValue(UserModel::class.java)
                enableOfflineSupport(userId)
                AppResponse.Success(userModel)
            } else {
                AppResponse.Success(null)
            }
        } catch (e: Exception) {
            Utils.printDebugLog("exception_while_fetching_user_data\n ${e.printStackTrace()}")
            AppResponse.Failure(e)
        }
    }

    suspend fun addUser(userName: String): AppResponse<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Utils.printDebugLog("Adding user - $userName")
                // Add the user data to Firebase Realtime Database
//                databaseReference.child("users")
//                    .child(userName)
//                    .setValue(UserModel(user_name = userName))
//                    .await()
                AppResponse.Success(true) // Return success if no exceptions occur
            } catch (e: Exception) {
                AppResponse.Failure(e) // Handle any exceptions
            }
        }
    }

    suspend fun searchUser(userName: String, userPassword: String): AppResponse<UserModel?> {
        return withContext(Dispatchers.IO) {
            try {
                Utils.printDebugLog("Searching user - $userName")
                val dataSnapshot = usersListDatabaseReference.child(userName).get().await()
                if (dataSnapshot.exists()) {
                    val userModel = dataSnapshot.getValue(UserModel::class.java)
                    if (userName == userModel?.userPassword) {
                        Utils.printDebugLog("user found- $userModel")
                        AppResponse.Success(userModel)
                    } else {
                        Utils.printDebugLog("user found- $userModel")
                        AppResponse.Success(null)
                    }
                } else {
                    AppResponse.Success(null) // User not found
                }
            } catch (e: Exception) {
                Utils.printDebugLog("getUserByName: $e")
                AppResponse.Failure(e)
            }
        }
    }

    fun saveLocation(userId: String, location: String, isCurrentLocation: Boolean, locationSavedListener: LocationSavedListener) {
        if (isCurrentLocation) {
            usersListDatabaseReference
                .child(userId)
                .child(FirebaseConstants.USER_SETTINGS)
                .child(FirebaseConstants.CURRENT_LOCATION)
                .setValue(location)
                .addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        locationSavedListener.onSuccess()
                    } else {
                        locationSavedListener.onError(result.exception)
                    }
                }
        } else {
            usersListDatabaseReference
                .child(userId)
                .child(FirebaseConstants.USER_SETTINGS)
                .child(FirebaseConstants.OTHER_LOCATIONS)
                .get()
                .addOnSuccessListener { snapshot ->
                    val existingList = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                    val updatedList = (existingList + location).toSet().toList()
                    usersListDatabaseReference
                        .child(userId)
                        .child(FirebaseConstants.USER_SETTINGS)
                        .child(FirebaseConstants.OTHER_LOCATIONS)
                        .setValue(updatedList)
                        .addOnCompleteListener {  result ->
                            if (result.isSuccessful) {
                                locationSavedListener.onSuccess()
                            } else {
                                locationSavedListener.onError(result.exception)
                            }
                        }
                }.addOnFailureListener {
                    locationSavedListener.onError(it)
                }
        }
    }

    suspend fun saveLocation(userId: String, location: String, isCurrentLocation: Boolean): AppResponse<Boolean> {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                val databaseRef = usersListDatabaseReference.child(userId).child(FirebaseConstants.USER_SETTINGS)

                if (isCurrentLocation) {
                    databaseRef.child(FirebaseConstants.CURRENT_LOCATION)
                        .setValue(location)
                        .addOnSuccessListener {
                            continuation.resume(AppResponse.Success(true))
                        }
                        .addOnFailureListener { exception ->
                            continuation.resume(AppResponse.Failure(exception))
                        }
                } else {
                    val otherLocationsRef = databaseRef.child(FirebaseConstants.OTHER_LOCATIONS)

                    otherLocationsRef.get().addOnSuccessListener { snapshot ->
                        val existingList = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                        val updatedList = (existingList + location).toSet().toList()

                        otherLocationsRef.setValue(updatedList)
                            .addOnSuccessListener {
                                continuation.resume(AppResponse.Success(true))
                            }
                            .addOnFailureListener { exception ->
                                continuation.resume(AppResponse.Failure(exception))
                            }
                    }.addOnFailureListener { exception ->
                        continuation.resume(AppResponse.Failure(exception))
                    }
                }
            }
        }
    }


    fun saveAndUpdateLocations(userId: String, newLocation: String, isCurrentLocation: Boolean): AppResponse<Boolean> {
        val future = CompletableFuture<AppResponse<Boolean>>()
        try {
            val userReference = usersListDatabaseReference.child(userId).child(FirebaseConstants.USER_SETTINGS).child("locations")

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
                            future.complete(AppResponse.Success(true))
                        } else {
                            future.complete(AppResponse.Failure(task.exception ?: UnknownException()))
                        }
                    }
            }.addOnFailureListener { exception ->
                future.complete(AppResponse.Failure(exception))
            }
        } catch (e: Exception) {
            future.complete(AppResponse.Failure(e))
        }
        return future.join()
    }


    suspend fun deleteLocation(userId: String, locationKey: String): FirebaseResponse<Boolean> {
        val future = CompletableFuture<FirebaseResponse<Boolean>>()
        try {
            Utils.printDebugLog("firebase manager location delete for user id: $userId")
            // Reference to the specific location node in the user's locations HashMap
            val userReference = usersListDatabaseReference
                .child(userId)
                .child(FirebaseConstants.USER_SETTINGS)
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
                usersListDatabaseReference.child(userId).child(FirebaseConstants.USER_SETTINGS)
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
                    usersListDatabaseReference.child(userId).child(FirebaseConstants.USER_SETTINGS)
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
                    usersListDatabaseReference.child(userId).child(FirebaseConstants.USER_SETTINGS)
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
            val userReference = usersListDatabaseReference.child(userId).child(FirebaseConstants.USER_SETTINGS)
            val updateMap = mapOf<String, Any>(FirebaseConstants.PREFERRED_UNIT to preferredUnit)
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
            val userReference = usersListDatabaseReference.child(uid)
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
