package com.shakir.weatherzoomer.ui.dashboard

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakir.weatherzoomer.exceptionHandling.exceptions.UserNotFoundException
import com.shakir.weatherzoomer.exceptionHandling.exceptions.UserStoredLocationNotFoundException
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.model.UserModel
import com.shakir.weatherzoomer.model.WeatherForecastModel
import com.shakir.weatherzoomer.repo.AppRepository
import com.shakir.weatherzoomer.response.AppResponse
import com.shakir.weatherzoomer.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(private val appRepository: AppRepository) : ViewModel() {

    private var userMLiveData = MutableLiveData<UserModel?>(null)
    val userLiveData: LiveData<UserModel?>
        get() = userMLiveData

    private var _isLocationDeletedMLiveData = MutableLiveData<FirebaseResponse<Boolean>>(null)
    val isLocationDeletedLiveData: LiveData<FirebaseResponse<Boolean>>
        get() = _isLocationDeletedMLiveData

    private var weatherMutableLiveData = MutableLiveData<AppResponse<WeatherForecastModel?>>(null)
    val weatherLiveData: LiveData<AppResponse<WeatherForecastModel?>>
        get() = weatherMutableLiveData

    private var weatherForecastModel: WeatherForecastModel? = null

    fun loadData() {
        Utils.printDebugLog("loadData_called")
        viewModelScope.launch {
            val currentUser = appRepository.getCurrentUserId().first()
            if (currentUser.isNotBlank()) {
                val userDataResponse = appRepository.getLoggedInUserData(currentUser)
                when (userDataResponse) {
                    is AppResponse.Success -> {
                        Utils.printDebugLog("userDataResponse viewmodel:: Success")
                        val userData = userDataResponse.data
                        userMLiveData.postValue(userData)
                        if (userData != null) {
                            val currentLocation = userData.userSettings.currentLocation
                            val otherLocations = userData.userSettings.otherLocations
                            val selectedLocation: String?
                            if (currentLocation != null) {
                                selectedLocation = currentLocation
                            } else if (otherLocations.isNotEmpty()) {
                                selectedLocation = otherLocations[0]
                            } else {
                                selectedLocation = null
                            }
                            if (!selectedLocation.isNullOrBlank()) {
                                val response = withContext(Dispatchers.IO) {
                                    appRepository.fetchWeatherData(selectedLocation, 3, "yes", "yes")
                                }
                                when(response) {
                                    is AppResponse.Success -> {
                                        weatherForecastModel = response.data
                                        weatherMutableLiveData.postValue(AppResponse.Success(response.data))
                                    }
                                    is AppResponse.Failure -> {
                                        weatherMutableLiveData.postValue(AppResponse.Failure(response.exception))
                                    }
                                }
                            } else {
                                weatherMutableLiveData.postValue(AppResponse.Failure(UserStoredLocationNotFoundException()))
                            }
                        } else {
                            weatherMutableLiveData.postValue(AppResponse.Failure(UserNotFoundException()))
                        }
                    }
                    is AppResponse.Failure -> {
                        weatherMutableLiveData.postValue(AppResponse.Failure(userDataResponse.exception))
                    }
                }
            } else {
                weatherMutableLiveData.postValue(AppResponse.Failure(UserNotFoundException()))
            }
        }
    }

    fun getWeatherData(): WeatherForecastModel? {
        return weatherForecastModel
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