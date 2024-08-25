package com.shakir.weatherzoomer.ui.takelocation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.model.LocationModel
import com.shakir.weatherzoomer.model.TakeGPSLocation
import com.shakir.weatherzoomer.repo.AppRepository
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
                            result.exception
                        )
                    )
                }
            } else if (currentlySignedInUser is FirebaseResponse.Failure) {
                _isPrimaryLocationUpdatedMLiveData.postValue(
                    FirebaseResponse.Failure(
                        currentlySignedInUser.exception
                    )
                )
            }
        }
    }

    fun addUserLocation(location: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentlySignedInUser = appRepository.getCurrentLoggedInUser()
            if (currentlySignedInUser is FirebaseResponse.Success && currentlySignedInUser.data != null) {
                val result = appRepository.addUserLocation(currentlySignedInUser.data.uid, location)
                if (result is FirebaseResponse.Success) {
                    _isPrimaryLocationUpdatedMLiveData.postValue(FirebaseResponse.Success(true))
                } else if (result is FirebaseResponse.Failure) {
                    _isPrimaryLocationUpdatedMLiveData.postValue(
                        FirebaseResponse.Failure(
                            result.exception
                        )
                    )
                }
            } else if (currentlySignedInUser is FirebaseResponse.Failure) {
                _isPrimaryLocationUpdatedMLiveData.postValue(
                    FirebaseResponse.Failure(
                        currentlySignedInUser.exception
                    )
                )
            }
        }
    }
    fun addUserLocation2(location: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentlySignedInUser = appRepository.getCurrentLoggedInUser()
            if (currentlySignedInUser is FirebaseResponse.Success && currentlySignedInUser.data != null) {
                val result = appRepository.addUserLocation2(currentlySignedInUser.data.uid, location)
                if (result is FirebaseResponse.Success) {
                    _isPrimaryLocationUpdatedMLiveData.postValue(FirebaseResponse.Success(true))
                } else if (result is FirebaseResponse.Failure) {
                    _isPrimaryLocationUpdatedMLiveData.postValue(
                        FirebaseResponse.Failure(
                            result.exception
                        )
                    )
                }
            } else if (currentlySignedInUser is FirebaseResponse.Failure) {
                _isPrimaryLocationUpdatedMLiveData.postValue(
                    FirebaseResponse.Failure(
                        currentlySignedInUser.exception
                    )
                )
            }
        }
    }

}