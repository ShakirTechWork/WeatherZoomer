package com.shakir.weatherzoomer.repo

import android.app.Activity
import android.content.Context
import com.shakir.weatherzoomer.BuildConfig
import com.shakir.weatherzoomer.api.ApiResponse
import com.shakir.weatherzoomer.api.NetworkEndpoints
import com.shakir.weatherzoomer.api.result
import com.shakir.weatherzoomer.datastore.AppDataStore
import com.shakir.weatherzoomer.firebase.FirebaseManager
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.firebase.firebaseAwaitOperationCaller
import com.shakir.weatherzoomer.model.AppRelatedData
import com.shakir.weatherzoomer.model.SelectedTimeModel
import com.shakir.weatherzoomer.model.UserModel
import com.shakir.weatherzoomer.model.WeatherData
import com.shakir.weatherzoomer.model.WeatherForecastModel
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.shakir.weatherzoomer.model.searchLocation.SearchLocationResultModel
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val networkEndpoints: NetworkEndpoints,
    private val appDataStore: AppDataStore,
    private val firebaseManager: FirebaseManager,
    private val applicationContext: Context
) {

    suspend fun createUserWithEmailAndPassword(email: String, password: String) =
        firebaseAwaitOperationCaller {
            firebaseManager.createUserWithEmailAndPassword(email, password)
        }

    suspend fun signInWithGoogleAccount(authCredential: AuthCredential) =
        firebaseAwaitOperationCaller {
            firebaseManager.signInWithGoogleAccount(authCredential)
        }

    suspend fun signInWithEmailAndPassword(email: String, password: String) =
        firebaseAwaitOperationCaller {
            firebaseManager.signInWithEmailAndPassword(email, password)
        }

    suspend fun addUserIntoFirebase(name: String, email: String): FirebaseResponse<Boolean> {
        return firebaseManager.addUserIntoDatabase(name, email)
    }

    suspend fun getCurrentLoggedInUser(): FirebaseResponse<FirebaseUser?> {
        return firebaseManager.getCurrentLoggedInUser()
    }

    suspend fun getUserData(userId: String): FirebaseResponse<UserModel?> {
        return firebaseManager.getUserData(userId)
    }

    suspend fun signOutCurrentUser(activity: Activity) {
        firebaseManager.signOutCurrentUser(activity)
    }

    fun updateUserPrimaryLocation(userId: String, primaryLocation: String): FirebaseResponse<Boolean> {
        return firebaseManager.updateUserPrimaryLocation(userId, primaryLocation)
    }

    fun updatePeriodicWeatherUpdatesData(userId: String, intervalInHours: Int, dndStartTime: Long, dndEndTime: Long): FirebaseResponse<Boolean> {
        return firebaseManager.updatePeriodicWeatherUpdatesData(userId, intervalInHours, dndStartTime, dndEndTime)
    }

    suspend fun updatePeriodicWeatherUpdatesData2(userId: String, intervalInHours: Int, dndStartTime: String, dndEndTime: String): FirebaseResponse<Boolean> {
        return firebaseManager.updatePeriodicWeatherUpdatesData2(userId, intervalInHours, dndStartTime, dndEndTime)
    }

    suspend fun updateTimelyWeatherUpdatesData(userId: String, timeList: ArrayList<SelectedTimeModel>): FirebaseResponse<Boolean> {
        return firebaseManager.updateTimelyWeatherUpdatesData(userId, timeList)
    }

    suspend fun updateUserUnitPreference(userId: String, preferredUnit: String): FirebaseResponse<Boolean> {
        return firebaseManager.updateUserUnitPreference(userId, preferredUnit)
    }

    suspend fun getAppRelatedData(): FirebaseResponse<AppRelatedData> {
        return firebaseManager.getAppRelatedData()
    }

    suspend fun saveUserPrimaryLocation(primaryLocation: String) {
        return appDataStore.savePrimaryLocation(primaryLocation)
    }

    suspend fun updateIsAppOpenedFirstTime(value: Boolean) {
        return appDataStore.updateIsAppOpenedFirstTime(value)
    }

    fun isAppOpenedFirstTime(): Flow<Boolean> {
        return appDataStore.isAppOpenedFirstTime
    }

    fun getUserPrimaryLocation(): Flow<String> {
        return appDataStore.userPrimaryLocation
    }

    suspend fun deleteAllUserDataFromDatastore() {
        appDataStore.deleteAllUserData()
    }

    suspend fun fetchLocationResults(query: String): Flow<ApiResponse<SearchLocationResultModel?>> = result {
        networkEndpoints.getSearchedLocationResults(BuildConfig.WEATHER_API_KEY, query)
    }

    suspend fun getCurrentWeatherData(
        location: String,
        aqi: String
    ): Flow<ApiResponse<WeatherData?>> = result {
        networkEndpoints.getCurrentWeather(BuildConfig.WEATHER_API_KEY, location, aqi)
    }

    fun getForecastData(
        location: String,
        days: Int,
        aqi: String,
        alerts: String
    ): Flow<ApiResponse<WeatherForecastModel?>> = result {
        networkEndpoints.forecastWeather(
            BuildConfig.WEATHER_API_KEY,
            location,
            days,
            aqi,
            alerts
        )
    }

}