package com.shakir.weatherzoomer.ui.walkthrough

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakir.weatherzoomer.repo.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class WalkThroughViewModel(private val appRepository: AppRepository) : ViewModel() {

    fun updateIsAppOpenedFirstTime(value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.updateIsAppOpenedFirstTime(value)
        }
    }

}