package com.example.weatherwish.ui.splashscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherwish.exceptionHandler.ExceptionErrorCodes
import com.example.weatherwish.exceptionHandler.ExceptionErrorMessages
import com.example.weatherwish.firebase.FirebaseResponse
import com.example.weatherwish.model.WeatherForecastModel
import com.example.weatherwish.repo.AppRepository
import com.example.weatherwish.utils.Utils
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SplashViewModel(private val appRepository: AppRepository) : ViewModel() {

    private val _currentLoggedInUserMutableLiveData = MutableLiveData<FirebaseResponse<Boolean>>()

    val currentLoggedInUserLiveData: LiveData<FirebaseResponse<Boolean>>
        get() = _currentLoggedInUserMutableLiveData

    init {
        getCurrentLoggedInUser()
    }

    private fun getCurrentLoggedInUser() {
        _currentLoggedInUserMutableLiveData.postValue(FirebaseResponse.Loading)
        viewModelScope.launch {
            val currentlySignedInUser = appRepository.getCurrentLoggedInUser()
            if (currentlySignedInUser is FirebaseResponse.Success) {
                if (currentlySignedInUser.data!=null) {
                    _currentLoggedInUserMutableLiveData.postValue(FirebaseResponse.Success(true))
                } else {
                    _currentLoggedInUserMutableLiveData.postValue(FirebaseResponse.Success(false))
                }
            } else if (currentlySignedInUser is FirebaseResponse.Failure) {
                _currentLoggedInUserMutableLiveData.postValue(
                    FirebaseResponse.Failure(
                        currentlySignedInUser.exception
                    )
                )
            }
        }
    }

    fun isAppOpenedFirstTime(): Flow<Boolean> {
        return appRepository.isAppOpenedFirstTime()
    }

}