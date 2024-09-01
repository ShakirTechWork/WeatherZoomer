package com.shakir.weatherzoomer.ui.dashboard

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakir.weatherzoomer.api.ApiResponse
import com.shakir.weatherzoomer.exceptionHandler.CustomException
import com.shakir.weatherzoomer.exceptionHandler.ExceptionErrorCodes
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.model.UserModel
import com.shakir.weatherzoomer.model.WeatherForecastModel
import com.shakir.weatherzoomer.repo.AppRepository
import com.shakir.weatherzoomer.utils.Utils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DashboardViewModel(private val appRepository: AppRepository) : ViewModel() {

    suspend fun getUserData(): FirebaseResponse<UserModel?> {
        val currentlySignedInUser = appRepository.getCurrentLoggedInUser()
        return if (currentlySignedInUser is FirebaseResponse.Success) {
            if (currentlySignedInUser.data != null) {
                Utils.printDebugLog("Found_Currently_Signed_in_User :: uid: ${currentlySignedInUser.data.uid}")
                val result = appRepository.getUserData(currentlySignedInUser.data.uid)
                if (result is FirebaseResponse.Success) {
                    Utils.printDebugLog("Fetched_User_Data :: ${result.data}")
                    FirebaseResponse.Success(result.data)
                } else if (result is FirebaseResponse.Failure) {
                    Utils.printErrorLog("Fetching_User_Data :: Failure: ${result.exception}")
                    FirebaseResponse.Failure(result.exception)
                } else {
                    Utils.printErrorLog("Fetching_User_Data :: Failure:")
                    FirebaseResponse.Failure(
                        CustomException(
                            ExceptionErrorCodes.UNKNOWN_EXCEPTION,
                            "Something went wrong!"
                        )
                    )
                }
            } else {
                FirebaseResponse.Failure(
                    CustomException(
                        ExceptionErrorCodes.UNKNOWN_EXCEPTION,
                        "Something went wrong!"
                    )
                )
            }
        } else if (currentlySignedInUser is FirebaseResponse.Failure) {
            FirebaseResponse.Failure(
                currentlySignedInUser.exception
            )
        } else {
            FirebaseResponse.Failure(
                CustomException(
                    ExceptionErrorCodes.UNKNOWN_EXCEPTION,
                    "Something went wrong!"
                )
            )
        }
    }

    fun getForecastData(
        location: String,
        numOfDays: Int,
        aqi: String,
        alerts: String
    ): Flow<ApiResponse<WeatherForecastModel?>> {
        return appRepository.getForecastData(location, numOfDays, aqi, alerts)
    }

    fun signOutCurrentUser(activity: Activity) {
        viewModelScope.launch {
            appRepository.signOutCurrentUser(activity)
        }
    }

    fun deleteSavedLocation(savedLocationsId: String) {
        viewModelScope.launch {
            val currentlySignedInUser = appRepository.getCurrentLoggedInUser()
            if (currentlySignedInUser is FirebaseResponse.Success && currentlySignedInUser.data != null) {
                val result = appRepository.deleteUserLocation(savedLocationsId, currentlySignedInUser.data.uid)
                if (result is FirebaseResponse.Success) {

                } else if (result is FirebaseResponse.Failure) {
//                    _isPrimaryLocationUpdatedMLiveData.postValue(
//                        FirebaseResponse.Failure(
//                            result.exception
//                        )
//                    )
                }
            } else if (currentlySignedInUser is FirebaseResponse.Failure) {
//                _isPrimaryLocationUpdatedMLiveData.postValue(
//                    FirebaseResponse.Failure(
//                        currentlySignedInUser.exception
//                    )
//                )
            }
        }
    }

}