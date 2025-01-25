package com.shakir.weatherzoomer.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shakir.weatherzoomer.repo.AppRepository

class LoginViewModelFactory(private val appRepository: AppRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModelFactory(appRepository) as T
    }

}