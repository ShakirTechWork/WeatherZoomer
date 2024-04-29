package com.shakir.weatherzoomer.ui.splashscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.model.AppRelatedData
import com.shakir.weatherzoomer.repo.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SplashViewModel(private val appRepository: AppRepository) : ViewModel() {

    private val _currentLoggedInUserMutableLiveData = MutableLiveData<FirebaseResponse<Boolean>>()

    val currentLoggedInUserLiveData: LiveData<FirebaseResponse<Boolean>>
        get() = _currentLoggedInUserMutableLiveData

    private val _appRelatedMLiveData = MutableLiveData<AppRelatedData?>()

    val appRelatedLiveData: LiveData<AppRelatedData?>
        get() = _appRelatedMLiveData

    init {
        getCurrentLoggedInUser()
    }

    fun updateAppRelatedData(appRelatedData: AppRelatedData) {
        _appRelatedMLiveData.postValue(appRelatedData)
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