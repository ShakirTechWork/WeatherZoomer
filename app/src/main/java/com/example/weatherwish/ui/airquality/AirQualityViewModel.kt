package com.example.weatherwish.ui.airquality

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherwish.api.ApiResponse
import com.example.weatherwish.model.WeatherData
import com.example.weatherwish.model.WeatherForecastModel
import com.example.weatherwish.repo.AppRepository
import com.example.weatherwish.utils.Utils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private const val TAG = "AirQualityViewModel"
class AirQualityViewModel(private val appRepository: AppRepository) : ViewModel() {

    private val airValuesListMutableLiveData = MutableLiveData<List<AirQualityData>>()

    val airValuesListLiveData: MutableLiveData<List<AirQualityData>>
        get() = airValuesListMutableLiveData

    private val airQualityMutableLiveData = MutableLiveData<String>()

    val airQualityLiveData: MutableLiveData<String>
        get() = airQualityMutableLiveData

    private val locationTImeMutableLiveData = MutableLiveData<String>()

    val locationTImeLiveData: MutableLiveData<String>
        get() = locationTImeMutableLiveData

    data class AirQualityData(
        val title: String,
        val value: String
    )

    suspend fun getCurrentWeatherData(location: String, aqi: String): Flow<ApiResponse<WeatherData?>> {
        return appRepository.getCurrentWeatherData(location, aqi)
    }

//    fun getAirQualityData(location: String){
//        val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
//            throwable.printStackTrace()
//        }
//        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
//            val result = appRepository.getCurrentWeather(location, "yes")
//            val airQuality = when (result.body()!!.current.air_quality.`us-epa-index`) {
//                1 -> "Good"
//                2 -> "Moderate"
//                3 -> "Unhealthy for sensitive group"
//                4 -> "Unhealthy"
//                5 -> "Very Unhealthy"
//                6 -> "Hazardous"
//                else -> {
//                    ""
//                }
//            }
//            airQualityMutableLiveData.postValue(airQuality)
//
//            locationTImeMutableLiveData.postValue("${result.body()!!.location.name}, Published at ${Utils.convertToHourTime(result.body()!!.location.localtime_epoch.toLong())}")
//
//            val airQualityDataList = listOf(
//                AirQualityData("Quality", "Values (μg/m3)"),
//                AirQualityData("Carbon Monoxide(co)", "${result.body()!!.current.air_quality.co}"),
//                AirQualityData("Nitrogen dioxide(no2)", "${result.body()!!.current.air_quality.no2}"),
//                AirQualityData("Ozone(o3)", "${result.body()!!.current.air_quality.o3}"),
//                AirQualityData("Sulphur dioxide(so2)", "${result.body()!!.current.air_quality.so2}"),
//                AirQualityData("PM2.5", "${result.body()!!.current.air_quality.pm2_5}"),
//                AirQualityData("PM10", "${result.body()!!.current.air_quality.pm10}"),
//                AirQualityData("US-EPA Standard", result.body()!!.current.air_quality.`us-epa-index`.toString()),
//                AirQualityData("UK Defra Index", result.body()!!.current.air_quality.`gb-defra-index`.toString())
//            )
//            airValuesListMutableLiveData.postValue(airQualityDataList)
//        }
//    }

}