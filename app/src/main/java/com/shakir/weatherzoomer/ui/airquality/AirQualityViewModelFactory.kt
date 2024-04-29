package com.shakir.weatherzoomer.ui.airquality

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shakir.weatherzoomer.repo.AppRepository

class AirQualityViewModelFactory(
    private val appRepository: AppRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AirQualityViewModel(appRepository) as T
    }

}