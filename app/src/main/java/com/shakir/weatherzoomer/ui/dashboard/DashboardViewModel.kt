package com.shakir.weatherzoomer.ui.dashboard

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(private val appRepository: AppRepository) : ViewModel() {

    private var _currentlySignedInUserMLiveData = MutableLiveData<FirebaseResponse<UserModel?>>(null)
    val currentlySignedInUserLiveData: LiveData<FirebaseResponse<UserModel?>>
        get() = _currentlySignedInUserMLiveData

    private var _weatherForecastMLiveData = MutableLiveData<ApiResponse<WeatherForecastModel?>>(null)
    val weatherForecastLiveData: LiveData<ApiResponse<WeatherForecastModel?>>
        get() = _weatherForecastMLiveData

    private var _isLocationDeletedMLiveData = MutableLiveData<FirebaseResponse<Boolean>>(null)
    val isLocationDeletedLiveData: LiveData<FirebaseResponse<Boolean>>
        get() = _isLocationDeletedMLiveData

    fun getCurrentlySignedInUserWithData() {
        viewModelScope.launch {
            _currentlySignedInUserMLiveData.postValue(FirebaseResponse.Loading)
            val currentSignedInUserResponse = appRepository.getCurrentLoggedInUser()
            when (currentSignedInUserResponse) {
                is FirebaseResponse.Success -> {
                    if (currentSignedInUserResponse.data != null) {
                        Utils.printDebugLog("currentSignedInUserResponse viewmodel:: Success | uid: ${currentSignedInUserResponse.data.uid}")
                        val userDataResponse = appRepository.getUserData(currentSignedInUserResponse.data.uid)
                        when (userDataResponse) {
                            is FirebaseResponse.Success -> {
                                Utils.printDebugLog("userDataResponse viewmodel:: Success")
                                _currentlySignedInUserMLiveData.postValue(userDataResponse)
                            }
                            is FirebaseResponse.Failure -> {
                                Utils.printErrorLog("userDataResponse viewmodel:: Failure: ${userDataResponse.exception}")
                                FirebaseResponse.Failure(userDataResponse.exception)
                            }
                            FirebaseResponse.Loading -> {
                                //no use here
                            }
                        }
                    } else {
                        FirebaseResponse.Failure(
                            CustomException(
                                ExceptionErrorCodes.UNKNOWN_EXCEPTION,
                                "Something went wrong!"
                            )
                        )
                    }
                }
                is FirebaseResponse.Failure -> {
                    FirebaseResponse.Failure(
                        currentSignedInUserResponse.exception
                    )
                }
                FirebaseResponse.Loading -> {
                    //no use here
                }
            }
        }
    }

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

    fun getWeatherForecastData(
        location: String,
        numOfDays: Int,
        aqi: String,
        alerts: String
    ) {
        viewModelScope.launch {
            _weatherForecastMLiveData.postValue(ApiResponse.Loading) // Emit loading state
            val response = withContext(Dispatchers.IO) {
                appRepository.getWeatherForecastData(location, numOfDays, aqi, alerts)
            }
            _weatherForecastMLiveData.postValue(response.value) // Post the success or failure state
        }
    }

    fun signOutCurrentUser(activity: Activity) {
        viewModelScope.launch {
            appRepository.signOutCurrentUser(activity)
        }
    }

    fun deleteLocation(locationId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentlySignedInUser = appRepository.getCurrentLoggedInUser()
            if (currentlySignedInUser is FirebaseResponse.Success && currentlySignedInUser.data != null) {
                Utils.printDebugLog("viewmodel delete")
                _isLocationDeletedMLiveData.postValue(appRepository.deleteLocation(currentlySignedInUser.data.uid, locationId))
            }
        }
    }

}