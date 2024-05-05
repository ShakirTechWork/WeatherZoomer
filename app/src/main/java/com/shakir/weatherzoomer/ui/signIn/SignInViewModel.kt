package com.shakir.weatherzoomer.ui.signIn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakir.weatherzoomer.firebase.FirebaseResponse
import com.shakir.weatherzoomer.repo.AppRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "SignInViewModel"
class SignInViewModel(private val appRepository: AppRepository) : ViewModel() {

    private val _resultMutableLiveData = MutableLiveData<FirebaseResponse<FirebaseUser>>()
    val resultMutableLiveData: MutableLiveData<FirebaseResponse<FirebaseUser>>
        get() = _resultMutableLiveData

    private val _privacyCheckUrlMLiveData = MutableLiveData("")
    val privacyCheckUrlLiveData: LiveData<String>
        get() = _privacyCheckUrlMLiveData

    private val _privacyCheckBoxMutableLiveData = MutableLiveData(false)

    val privacyCheckBoxLiveData: LiveData<Boolean>
        get() = _privacyCheckBoxMutableLiveData

    fun setPrivacyUrl(url: String) {
        _privacyCheckUrlMLiveData.postValue(url)
    }

    fun setPrivacyCheckBoxChecked(boolean: Boolean) {
        _privacyCheckBoxMutableLiveData.postValue(boolean)
    }

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