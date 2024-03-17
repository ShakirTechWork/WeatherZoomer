package com.example.weatherwish.ui.signIn

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherwish.firebase.FirebaseResponse
import com.example.weatherwish.repo.AppRepository
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

private const val TAG = "SignInViewModel"
class SignInViewModel(private val appRepository: AppRepository) : ViewModel() {

    private val _resultMutableLiveData = MutableLiveData<FirebaseResponse<FirebaseUser>>()

    val resultMutableLiveData: MutableLiveData<FirebaseResponse<FirebaseUser>>
        get() = _resultMutableLiveData

    private val _errorLiveMutableLiveData = MutableLiveData<FirebaseResponse<FirebaseUser>>()

    val errorLiveMutableLiveData: MutableLiveData<FirebaseResponse<FirebaseUser>>
        get() = _errorLiveMutableLiveData

    suspend fun signInWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _resultMutableLiveData.postValue(FirebaseResponse.Loading)
            val result = appRepository.signInWithEmailAndPassword(email, password)
            if (result is FirebaseResponse.Success) {
                val user = result.data?.user
                if (user != null) {
                    _resultMutableLiveData.postValue(FirebaseResponse.Success(user))
                } else {
                    _resultMutableLiveData.postValue(
                        FirebaseResponse.Failure(null)
                    )
                }
            } else if (result is FirebaseResponse.Failure) {
                _resultMutableLiveData.postValue(
                    FirebaseResponse.Failure(result.exception)
                )
            }
        }
    }

}