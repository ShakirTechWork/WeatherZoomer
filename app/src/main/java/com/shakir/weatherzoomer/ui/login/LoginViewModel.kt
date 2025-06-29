package com.shakir.weatherzoomer.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shakir.weatherzoomer.model.UserModel
import com.shakir.weatherzoomer.repo.AppRepository
import com.shakir.weatherzoomer.response.AppResponse

class LoginViewModel(private val appRepository: AppRepository): ViewModel() {

    private val resultMutableLiveData = MutableLiveData<AppResponse<UserModel?>>()

    val resultLiveData: LiveData<AppResponse<UserModel?>>
        get() = resultMutableLiveData

}