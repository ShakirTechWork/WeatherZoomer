package com.shakir.weatherzoomer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shakir.weatherzoomer.model.UserModel

class SharedViewModel : ViewModel() {
    var userData: UserModel? = null
    var appPlayStoreLink: String = ""
    var privacyPolicyUrl: String = ""

    private var _deleteSavedLocationMLiveData = MutableLiveData("")
    val deleteSavedLocationLiveData: LiveData<String>
        get() = _deleteSavedLocationMLiveData

    private var _isLocationSelectedMLiveData = MutableLiveData<String?>(null)
    val isLocationSelectedLiveData: LiveData<String?>
        get() = _isLocationSelectedMLiveData

    private var _onNewLocationRequestedMLiveData = MutableLiveData<Boolean>(false)
    val onNewLocationRequestedLiveData : LiveData<Boolean>
        get() = _onNewLocationRequestedMLiveData

    fun deleteLocationAtIndex(locationId: String) {
        _deleteSavedLocationMLiveData.postValue(locationId)
    }

    fun selectLocation(location: String?) {
        _isLocationSelectedMLiveData.postValue(location)
    }

    fun onNewLocationRequested() {
        _onNewLocationRequestedMLiveData.postValue(true)
    }

}