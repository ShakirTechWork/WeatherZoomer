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

    private val _userDataMLiveData = MutableLiveData<UserModel?>()

    val userDataLiveData: LiveData<UserModel?>
        get() = _userDataMLiveData

    private val forecastWeatherMutableLiveData = MutableLiveData<WeatherForecastModel>()

    val forecastWeatherLiveData: LiveData<WeatherForecastModel>
        get() = forecastWeatherMutableLiveData

    private val airQualityIndexMLiveData = MutableLiveData<String>()

    val airQualityIndexLiveData: LiveData<String>
        get() = airQualityIndexMLiveData

//    fun getForecastData(location: String, numOfDays: Int, aqi: String, alerts: String) {
//        val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
//            throwable.printStackTrace()
//        }
//        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
//            Log.d("TAG", "calling_api: ")
//            val result = appRepository.getForecastData(location, numOfDays, aqi, alerts)
//            val resultBody = result.body()!!
//            forecastWeatherMutableLiveData.postValue(result.body())
//            val airQuality = when (resultBody.current.air_quality.`us-epa-index`) {
//                1 -> "Good"
//                2 -> "Moderate"
//                3 -> "Unhealthy for sensitive group"
//                4 -> "Unhealthy"
//                5 -> "Very Unhealthy"
//                6 -> "Hazardous"
//                else -> {
//                    ""
//                }
//            }
//            airQualityIndexMLiveData.postValue(airQuality)
//        }
//    }

    fun getUserPrimaryLocation(): Flow<String> {
        return appRepository.getUserPrimaryLocation()
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
                    FirebaseResponse.Failure(
                        ExceptionErrorCodes.FIREBASE_EXCEPTION,
                        ExceptionErrorMessages.FIREBASE_EXCEPTION,
                        CustomException(
                            ExceptionErrorCodes.UNKNOWN_EXCEPTION,
                            "Something went wrong!"
                        )
                    )
                } else {
                    Utils.printErrorLog("Fetching_User_Data :: Failure:")
                    FirebaseResponse.Failure(
                        ExceptionErrorCodes.FIREBASE_EXCEPTION,
                        ExceptionErrorMessages.FIREBASE_EXCEPTION,
                        CustomException(
                            ExceptionErrorCodes.UNKNOWN_EXCEPTION,
                            "Something went wrong!"
                        )
                    )
                }
            } else {
                FirebaseResponse.Failure(
                    ExceptionErrorCodes.FIREBASE_EXCEPTION,
                    ExceptionErrorMessages.FIREBASE_EXCEPTION,
                    CustomException(
                        ExceptionErrorCodes.UNKNOWN_EXCEPTION,
                        "Something went wrong!"
                    )
                )
            }
        } else if (currentlySignedInUser is FirebaseResponse.Failure) {
            FirebaseResponse.Failure(
                ExceptionErrorCodes.FIREBASE_EXCEPTION,
                ExceptionErrorMessages.FIREBASE_EXCEPTION,
                currentlySignedInUser.exception
            )
        } else {
            FirebaseResponse.Failure(
                ExceptionErrorCodes.FIREBASE_EXCEPTION,
                ExceptionErrorMessages.FIREBASE_EXCEPTION,
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