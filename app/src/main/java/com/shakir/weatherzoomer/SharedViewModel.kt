package com.shakir.weatherzoomer

import androidx.lifecycle.ViewModel
import com.shakir.weatherzoomer.model.UserModel

class SharedViewModel : ViewModel() {
    var userData: UserModel? = null
    var appPlayStoreLink: String = ""
}