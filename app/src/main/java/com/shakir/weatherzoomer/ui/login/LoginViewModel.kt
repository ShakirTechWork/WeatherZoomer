package com.shakir.weatherzoomer.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.model.UserModel
import com.shakir.weatherzoomer.repo.AppRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val appRepository: AppRepository): ViewModel() {

    private val resultMutableLiveData = MutableLiveData<FirebaseResponse<UserModel>>()

    val resultLiveData: LiveData<FirebaseResponse<UserModel>>
        get() = resultMutableLiveData

    fun loginUserByName(userName: String) {
        viewModelScope.launch {
            val result = appRepository.loginUserByName(userName)
        }
    }

}