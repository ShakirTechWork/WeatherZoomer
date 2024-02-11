package com.example.weatherwish

import androidx.lifecycle.ViewModel
import com.example.weatherwish.model.UserModel

class SharedViewModel : ViewModel() {
    var userData: UserModel? = null
}