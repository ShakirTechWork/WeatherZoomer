package com.shakir.weatherzoomer.ui.takelocation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakir.weatherzoomer.exceptionHandling.exceptions.UserNotFoundException
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.model.TakeGPSLocation
import com.shakir.weatherzoomer.repo.AppRepository
import com.shakir.weatherzoomer.response.AppResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class LocationViewModel(private val appRepository: AppRepository) : ViewModel() {

    private val _gpsLocationLiveData = MutableLiveData<TakeGPSLocation>()
    val gpsLocationLiveData: LiveData<TakeGPSLocation> get() = _gpsLocationLiveData

    private val _isLocationSavedMLiveData = MutableLiveData<AppResponse<Boolean>>()
    val isLocationSavedLiveData: LiveData<AppResponse<Boolean>>
        get() = _isLocationSavedMLiveData


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

    fun saveLocation(location: String, isCurrentLocation: Boolean) {
        viewModelScope.launch {
            val userId = appRepository.getCurrentUserId().firstOrNull()
            if (!userId.isNullOrBlank()) {
                _isLocationSavedMLiveData.postValue(appRepository.saveLocation(userId, location, isCurrentLocation))
            } else {
                _isLocationSavedMLiveData.postValue(AppResponse.Failure(UserNotFoundException()))
            }
        }
    }

}