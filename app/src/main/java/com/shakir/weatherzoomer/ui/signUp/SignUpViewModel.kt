package com.shakir.weatherzoomer.ui.signUp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.repo.AppRepository
import com.shakir.weatherzoomer.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class SignUpViewModel(private val appRepository: AppRepository) : ViewModel() {

    private val _resultMutableLiveData = MutableLiveData<FirebaseResponse<Boolean>>()

    val resultMutableLiveData: LiveData<FirebaseResponse<Boolean>>
        get() = _resultMutableLiveData

    suspend fun createUserWithEmailAndPassword(name: String, email: String, password: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _resultMutableLiveData.postValue(FirebaseResponse.Loading)
            val result = appRepository.createUserWithEmailAndPassword(email, password)
            if (result is FirebaseResponse.Success) {
                val token = result.data?.user?.getIdToken(true)?.await()?.token
                if (token != null && token.isNotBlank()) {
                    Utils.printDebugLog("Adding_User_In_DB :: Loading")
                    val response = addUserIntoFirebase(name, email)
                    if (response is FirebaseResponse.Success) {
                        Utils.printDebugLog("Adding_User_In_DB :: Success")
                        _resultMutableLiveData.postValue(FirebaseResponse.Success(true))
                    } else if (response is FirebaseResponse.Failure) {
                        Utils.printDebugLog("Adding_User_In_DB :: Failure")
                        _resultMutableLiveData.postValue(FirebaseResponse.Failure(response.exception))
                    }
                } else {
                    _resultMutableLiveData.postValue(FirebaseResponse.Failure(Exception()))
                }
            } else if (result is FirebaseResponse.Failure) {
                _resultMutableLiveData.postValue(FirebaseResponse.Failure(result.exception))
            }
        }
    }

    suspend fun addUserIntoFirebase(name: String, email: String): FirebaseResponse<Boolean> {
        return appRepository.addUserIntoFirebase(name, email)
    }

}