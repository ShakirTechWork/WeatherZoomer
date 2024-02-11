package com.example.weatherwish.ui.signUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherwish.repo.AppRepository
import com.example.weatherwish.ui.splashscreen.SplashViewModel

class SignUpViewModelFactory(private val appRepository: AppRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SignUpViewModel(appRepository) as T
    }

}