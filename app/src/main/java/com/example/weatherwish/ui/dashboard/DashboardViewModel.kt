package com.example.weatherwish.ui.dashboard

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherwish.R
import com.example.weatherwish.api.ApiResponse
import com.example.weatherwish.constants.UVIndex
import com.example.weatherwish.exceptionHandler.CustomException
import com.example.weatherwish.exceptionHandler.ExceptionErrorCodes
import com.example.weatherwish.exceptionHandler.ExceptionErrorMessages
import com.example.weatherwish.firebase.FirebaseResponse
import com.example.weatherwish.model.UserModel
import com.example.weatherwish.model.WeatherData
import com.example.weatherwish.model.WeatherForecastModel
import com.example.weatherwish.repo.AppRepository
import com.example.weatherwish.utils.Utils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
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

}