package com.example.weatherwish.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherwish.repo.AppRepository

class SettingsViewModelFactory(private val appRepository: AppRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(appRepository) as T
    }

}