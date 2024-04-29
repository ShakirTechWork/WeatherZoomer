package com.shakir.weatherzoomer.ui.walkthrough

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shakir.weatherzoomer.repo.AppRepository

class WalkThroughViewModelFactory(private val appRepository: AppRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WalkThroughViewModel(appRepository) as T
    }

}