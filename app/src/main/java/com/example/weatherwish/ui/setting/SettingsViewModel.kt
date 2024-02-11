package com.example.weatherwish.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherwish.firebase.FirebaseResponse
import com.example.weatherwish.repo.AppRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val appRepository: AppRepository) : ViewModel() {

    fun updateUserUnitPreference(preferredUnit: String) {
        viewModelScope.launch {
            val userId = appRepository.getCurrentLoggedInUser()
            if (userId is FirebaseResponse.Success) {
                if (userId.data != null) {
                    appRepository.updateUserUnitPreference(userId.data.uid, preferredUnit)
                }
            }
        }
    }

    fun signOutCurrentUser() {
        viewModelScope.launch {
//            appRepository.deleteAllUserDataFromDatastore()
            appRepository.signOutCurrentUser()
        }
    }

}