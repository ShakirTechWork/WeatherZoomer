package com.example.weatherwish.ui.walkthrough

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherwish.repo.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WalkThroughViewModel(private val appRepository: AppRepository) : ViewModel() {

    fun updateIsAppOpenedFirstTime(value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.updateIsAppOpenedFirstTime(value)
        }
    }

}