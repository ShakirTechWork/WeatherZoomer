package com.example.weatherwish.ui.walkthrough

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherwish.repo.AppRepository
import com.example.weatherwish.ui.takelocation.LocationViewModel

class WalkThroughViewModelFactory(private val appRepository: AppRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WalkThroughViewModel(appRepository) as T
    }

}