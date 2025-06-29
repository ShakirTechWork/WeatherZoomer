package com.shakir.weatherzoomer.ui.splashscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakir.weatherzoomer.repo.AppRepository
import com.shakir.weatherzoomer.utils.Utils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(private val appRepository: AppRepository) : ViewModel() {

    private val nextScreenMutableLiveData = MutableLiveData<NextScreen>()

    val nextScreenLiveData: LiveData<NextScreen>
        get() = nextScreenMutableLiveData

    fun decideNextScreen() {
        viewModelScope.launch {
            val isWalkThroughScreenSeen = appRepository.isWalkThroughScreenSeen().first()
            if (isWalkThroughScreenSeen) {
                val userId = appRepository.getCurrentUserId().first()
                Utils.printDebugLog("userId: $userId")
                if (userId.isNotBlank()) {
                    nextScreenMutableLiveData.postValue(NextScreen.MAIN_ACTIVITY)
                } else {
                    nextScreenMutableLiveData.postValue(NextScreen.WALK_THROUGH_ACTIVITY)
                }
            } else {
                nextScreenMutableLiveData.postValue(NextScreen.WALK_THROUGH_ACTIVITY)
            }
        }
    }

}