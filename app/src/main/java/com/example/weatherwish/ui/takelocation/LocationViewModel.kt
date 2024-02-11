package com.example.weatherwish.ui.takelocation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherwish.exceptionHandler.CustomException
import com.example.weatherwish.exceptionHandler.ExceptionErrorCodes
import com.example.weatherwish.exceptionHandler.ExceptionErrorMessages
import com.example.weatherwish.firebase.FirebaseResponse
import com.example.weatherwish.model.TakeGPSLocation
import com.example.weatherwish.repo.AppRepository
import com.example.weatherwish.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationViewModel(private val appRepository: AppRepository) : ViewModel() {

    private val _gpsLocationLiveData = MutableLiveData<TakeGPSLocation>()
    val gpsLocationLiveData: LiveData<TakeGPSLocation> get() = _gpsLocationLiveData

    private val _isPrimaryLocationUpdatedMLiveData = MutableLiveData<FirebaseResponse<Boolean>>()
    val isPrimaryLocationUpdatedLiveData: LiveData<FirebaseResponse<Boolean>>
        get() = _isPrimaryLocationUpdatedMLiveData


    fun updateGpsStatus(isGpsOn: Boolean) {
        val currentData = _gpsLocationLiveData.value ?: TakeGPSLocation(false, false)
        currentData.isGpsOn = isGpsOn
        _gpsLocationLiveData.value = currentData
    }

    fun updateLocationPermissionStatus(isLocationPermissionGranted: Boolean) {
        val currentData = _gpsLocationLiveData.value ?: TakeGPSLocation(false, false)
        currentData.isLocationPermissionGranted = isLocationPermissionGranted
        _gpsLocationLiveData.value = currentData
    }

    private suspend fun storePrimaryLocationLocally(location: String) {
        appRepository.saveUserPrimaryLocation(location)
    }

    fun updateUserPrimaryLocation(
        primaryLocation: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentlySignedInUser = appRepository.getCurrentLoggedInUser()
            if (currentlySignedInUser is FirebaseResponse.Success && currentlySignedInUser.data != null) {
                val result = appRepository.updateUserPrimaryLocation(
                    currentlySignedInUser.data.uid,
                    primaryLocation
                )
                if (result is FirebaseResponse.Success) {
                    storePrimaryLocationLocally(primaryLocation)
                    _isPrimaryLocationUpdatedMLiveData.postValue(FirebaseResponse.Success(true))
                } else if (result is FirebaseResponse.Failure) {
                    _isPrimaryLocationUpdatedMLiveData.postValue(
                        FirebaseResponse.Failure(
                            ExceptionErrorCodes.FIREBASE_EXCEPTION,
                            ExceptionErrorMessages.FIREBASE_EXCEPTION,
                            result.exception
                        )
                    )
                }
            } else if (currentlySignedInUser is FirebaseResponse.Failure) {
                _isPrimaryLocationUpdatedMLiveData.postValue(
                    FirebaseResponse.Failure(
                        ExceptionErrorCodes.FIREBASE_EXCEPTION,
                        ExceptionErrorMessages.FIREBASE_EXCEPTION,
                        currentlySignedInUser.exception
                    )
                )
            }
        }
    }

}