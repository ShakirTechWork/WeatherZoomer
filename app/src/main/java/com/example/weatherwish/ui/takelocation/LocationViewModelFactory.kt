package com.example.weatherwish.ui.takelocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherwish.repo.AppRepository

class LocationViewModelFactory(private val appRepository: AppRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocationViewModel(appRepository) as T
    }

}