package com.shakir.weatherzoomer.ui.takelocation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shakir.weatherzoomer.api.ApiResponse
import com.shakir.weatherzoomer.model.searchLocation.SearchLocationResultModel
import com.shakir.weatherzoomer.repo.AppRepository

class SearchLocationViewModel(private val repository: AppRepository): ViewModel() {

    private val _searchResultListMLiveData = MutableLiveData<ApiResponse<SearchLocationResultModel?>?>()
    val searchResultLiveData: LiveData<ApiResponse<SearchLocationResultModel?>?>
        get() = _searchResultListMLiveData

    suspend fun getSearchResults(query: String) {
        repository.fetchLocationResults(query).collect() {
            when (it) {
                is ApiResponse.Success -> {
                    val location = it.data
                    if (location != null) {
                        _searchResultListMLiveData.postValue(it)
                    } else {
                        _searchResultListMLiveData.postValue(null)
                    }
                }
                is ApiResponse.Failure -> {
                    _searchResultListMLiveData.postValue(null)
                }
                is ApiResponse.Loading -> {

                }
            }
        }
    }

}