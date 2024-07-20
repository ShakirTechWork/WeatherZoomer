package com.shakir.weatherzoomer.ui.takelocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shakir.weatherzoomer.repo.AppRepository

class SearchLocationViewModelFactory(private val appRepository: AppRepository): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchLocationViewModel(appRepository) as T
    }

}