package com.example.weatherwish.ui.periodicWeatherUpdates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherwish.repo.AppRepository

class WeatherUpdatesViewModelFactory(private val appRepository: AppRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherUpdatesViewModel(appRepository) as T
    }

}