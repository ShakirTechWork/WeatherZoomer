package com.example.weatherwish.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherwish.repo.AppRepository

class DashboardViewModelFactory(private val appRepository: AppRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(appRepository) as T
    }

}