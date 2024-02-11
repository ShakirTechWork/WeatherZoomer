package com.example.weatherwish.ui.airquality

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherwish.repo.AppRepository

class AirQualityViewModelFactory(
    private val appRepository: AppRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AirQualityViewModel(appRepository) as T
    }

}