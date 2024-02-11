package com.example.weatherwish.ui.signIn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.weatherwish.repo.AppRepository
import com.example.weatherwish.ui.signUp.SignUpViewModel

class SignInViewModelFactory(private val appRepository: AppRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SignInViewModel(appRepository) as T
    }

}