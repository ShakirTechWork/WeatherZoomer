package com.shakir.weatherzoomer.ui.walkthrough

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakir.weatherzoomer.model.UserModel
import com.shakir.weatherzoomer.repo.AppRepository
import com.shakir.weatherzoomer.response.AppResponse
import com.shakir.weatherzoomer.userSession.UserSessionManager
import kotlinx.coroutines.launch


class WalkThroughViewModel(private val appRepository: AppRepository) : ViewModel() {

    private val userRegistrationMutableLiveData = MutableLiveData<AppResponse<String?>>()
    val userRegistrationLiveData: LiveData<AppResponse<String?>>
        get() = userRegistrationMutableLiveData

    fun registerUser() {
        viewModelScope.launch {
            UserSessionManager.registerUser { result ->
                when (result) {
                    is AppResponse.Success -> {
                        if (result.data != null) {
                            userRegistrationMutableLiveData.postValue(AppResponse.Success(result.data))
                        }
                    }
                    is AppResponse.Failure -> {
                        userRegistrationMutableLiveData.postValue(AppResponse.Failure(result.exception))
                    }
                }
            }
        }
    }

    fun storeUser(userId: String) {
        appRepository.storeUser(userId)
    }

    fun walkthroughScreenSeen() {
        appRepository.walkthroughScreenSeen()
    }

}