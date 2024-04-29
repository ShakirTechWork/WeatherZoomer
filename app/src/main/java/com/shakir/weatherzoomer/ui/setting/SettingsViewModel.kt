package com.shakir.weatherzoomer.ui.setting

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.repo.AppRepository
import com.shakir.weatherzoomer.utils.Utils
import kotlinx.coroutines.launch

class SettingsViewModel(private val appRepository: AppRepository) : ViewModel() {

    private val _isUnitPreferenceUpdatedMLiveData = MutableLiveData<FirebaseResponse<Boolean>>()

    val isUnitPreferenceUpdatedLiveData: MutableLiveData<FirebaseResponse<Boolean>>
        get() = _isUnitPreferenceUpdatedMLiveData

    fun updateUserUnitPreference(preferredUnit: String) {
        viewModelScope.launch {
            val userId = appRepository.getCurrentLoggedInUser()
            if (userId is FirebaseResponse.Success) {
                if (userId.data != null) {
                    Utils.printDebugLog("preferredUnit: $preferredUnit")
                    val result = appRepository.updateUserUnitPreference(userId.data.uid, preferredUnit)
                    when (result) {
                        is FirebaseResponse.Success -> {
                            _isUnitPreferenceUpdatedMLiveData.postValue(FirebaseResponse.Success(true))
                        }

                        is FirebaseResponse.Failure -> {
                            _isUnitPreferenceUpdatedMLiveData.postValue(FirebaseResponse.Failure(result.exception))
                        }

                        else -> {
                            FirebaseResponse.Failure(Exception("Something went wrong!"))
                        }
                    }
                }
            }
        }
    }

    fun signOutCurrentUser(activity: Activity) {
        viewModelScope.launch {
//            appRepository.deleteAllUserDataFromDatastore()
            appRepository.signOutCurrentUser(activity)
        }
    }

}