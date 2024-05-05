package com.shakir.weatherzoomer.ui.walkthrough

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakir.weatherzoomer.repo.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WalkThroughViewModel(private val appRepository: AppRepository) : ViewModel() {

    fun updateIsAppOpenedFirstTime(value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.updateIsAppOpenedFirstTime(value)
        }
    }

    fun signOutCurrentUser(activity: Activity) {
        viewModelScope.launch {
//            appRepository.deleteAllUserDataFromDatastore()
            appRepository.signOutCurrentUser(activity)
        }
    }

}