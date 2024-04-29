package com.shakir.weatherzoomer.ui.takelocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shakir.weatherzoomer.repo.AppRepository

class LocationViewModelFactory(private val appRepository: AppRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocationViewModel(appRepository) as T
    }

}