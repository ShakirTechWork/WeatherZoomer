package com.example.weatherwish.ui.periodicWeatherUpdates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherwish.firebase.FirebaseResponse
import com.example.weatherwish.model.SelectedTimeModel
import com.example.weatherwish.model.WeatherForecastModel
import com.example.weatherwish.repo.AppRepository
import com.example.weatherwish.utils.Utils
import kotlinx.coroutines.launch

class WeatherUpdatesViewModel(private val appRepository: AppRepository) : ViewModel() {

    val updateTypeMutableLiveData = MutableLiveData<String>()

    val updateTypeLiveData: LiveData<String>
        get() = updateTypeMutableLiveData

    private val periodicButtonStateMLiveData = MutableLiveData<String>()

    val periodicButtonStateLiveData: LiveData<String>
        get() = periodicButtonStateMLiveData

    fun setPeriodicButtonState(state: String) {
        Utils.printDebugLog("state: $state")
        periodicButtonStateMLiveData.value = state
    }

    fun updatePeriodicWeatherUpdatesData(
        intervalInHours: Int,
        dndStartTime: String,
        dndEndTime: String
    ) {
        viewModelScope.launch {
            val userId = appRepository.getCurrentLoggedInUser()
            Utils.printDebugLog("userId1234: $userId")
            if (userId is FirebaseResponse.Success) {
                if (userId.data != null) {
//                    val result = appRepository.updatePeriodicWeatherUpdatesData(
//                        userId.data.uid,
//                        intervalInHours,
//                        dndStartTime,
//                        dndEndTime
//                    )
                    val result = appRepository.updatePeriodicWeatherUpdatesData2(
                        userId.data.uid,
                        intervalInHours,
                        dndStartTime,
                        dndEndTime
                    )
                    if (result is FirebaseResponse.Success) {
                        Utils.printDebugLog("updatePeriodicWeatherUpdatesData Success: ${result}")
                    } else if (result is FirebaseResponse.Failure) {
                        Utils.printDebugLog("updatePeriodicWeatherUpdatesData failure: ${result.exception}")
                    } else {
                        Utils.printDebugLog("updatePeriodicWeatherUpdatesData loading")
                    }
                }
            }
        }
    }

    fun updateTimelyWeatherUpdatesData(timeList: ArrayList<SelectedTimeModel>) {
        viewModelScope.launch {
            val userId = appRepository.getCurrentLoggedInUser()
            Utils.printDebugLog("CurrentLoggedInUser: $userId")
            if (userId is FirebaseResponse.Success) {
                if (userId.data != null) {
                    val result = appRepository.updateTimelyWeatherUpdatesData(
                        userId.data.uid,
                        timeList
                    )
                    if (result is FirebaseResponse.Success) {
                        Utils.printDebugLog("updatePeriodicWeatherUpdatesData Success: ${result}")
                    } else if (result is FirebaseResponse.Failure) {
                        Utils.printDebugLog("updatePeriodicWeatherUpdatesData failure: ${result.exception}")
                    } else {
                        Utils.printDebugLog("updatePeriodicWeatherUpdatesData loading")
                    }
                }
            }
        }
    }

}