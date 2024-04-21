package com.example.weatherwish.appRelatedData

import androidx.lifecycle.MutableLiveData
import com.example.weatherwish.firebase.FirebaseResponse
import com.example.weatherwish.model.AppRelatedData
import com.example.weatherwish.repo.AppRepository
import com.example.weatherwish.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppDataManager(private val appRepository: AppRepository) {

    val appRelatedDataLiveData: MutableLiveData<AppRelatedData?> = MutableLiveData()

    fun fetchAppRelatedData() {
        CoroutineScope(Dispatchers.IO).launch {
            Utils.printDebugLog("fetchAppRelatedData:: Loading")
            val data = appRepository.getAppRelatedData()
            when (data) {
                is FirebaseResponse.Success -> {
                    if (data.data != null) {
                        appRelatedDataLiveData.postValue(data.data)
                        Utils.printDebugLog("fetchAppRelatedData:: Success | App_version: ${data.data}")
                    } else {
                        Utils.printDebugLog("fetchAppRelatedData:: Success | but got null")
                    }
                }
                is FirebaseResponse.Failure -> {
                    Utils.printDebugLog("fetchAppRelatedData:: Failed | exception: ${data.exception}")
                    appRelatedDataLiveData.postValue(null)
                }
                FirebaseResponse.Loading -> {}
            }
        }
    }
}
