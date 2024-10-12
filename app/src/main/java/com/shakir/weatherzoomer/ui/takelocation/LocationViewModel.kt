package com.shakir.weatherzoomer.ui.takelocation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.model.TakeGPSLocation
import com.shakir.weatherzoomer.repo.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationViewModel(private val appRepository: AppRepository) : ViewModel() {

    private val _gpsLocationLiveData = MutableLiveData<TakeGPSLocation>()
    val gpsLocationLiveData: LiveData<TakeGPSLocation> get() = _gpsLocationLiveData

    private val _isLocationSavedMLiveData = MutableLiveData<FirebaseResponse<Boolean>>()
    val isLocationSavedLiveData: LiveData<FirebaseResponse<Boolean>>
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

    fun saveLocation(location: String, isCurrentLocation: Boolean, isSelectedLocation: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentlyLoggedInUserResponse = appRepository.getCurrentLoggedInUser()
            when (currentlyLoggedInUserResponse) {
                is FirebaseResponse.Success -> {
                    if (currentlyLoggedInUserResponse.data != null) {
                        _isLocationSavedMLiveData.postValue(appRepository.saveLocation(
                            currentlyLoggedInUserResponse.data.uid,
                            location, isCurrentLocation))
                    }
                }
                is FirebaseResponse.Failure -> {

                }
                FirebaseResponse.Loading -> {}
            }
        }
    }

}